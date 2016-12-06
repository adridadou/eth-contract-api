package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractRpc;
import org.adridadou.ethereum.values.*;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyRpc implements BlockchainProxy {

    private static final int SLEEP_DURATION = 5000;
    private static final int ATTEMPTS = 120;
    private static final Logger log = LoggerFactory.getLogger(BlockchainProxyRpc.class);
    private final Map<EthAccount, BigInteger> pendingTransactions = new CopyOnWriteMap<>();
    private final ChainId chainId;

    private final Web3JFacade web3JFacade;

    public BlockchainProxyRpc(Web3JFacade web3jFacade, ChainId chainId) {
        this.web3JFacade = web3jFacade;
        this.chainId = chainId;
    }

    @Override
    public SmartContract map(SoliditySource src, String contractName, EthAddress address, EthAccount sender) {
        CompilationResult.ContractMetadata metadata;
        try {
            metadata = compile(src, contractName);
            return mapFromAbi(new ContractAbi(metadata.abi), address, sender);

        } catch (IOException e) {
            throw new EthereumApiException("error while mapping a smart contract", e);
        }
    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractRpc(abi.getAbi(), web3JFacade, sender, address, this);
    }

    @Override
    public CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs) {
        try {
            return createContract(code, contractName, sender, constructorArgs).thenApply(SmartContractRpc::getAddress);
        } catch (IOException e) {
            throw new EthereumApiException("error while publishing " + contractName + ":", e);
        }
    }

    private CompletableFuture<SmartContractRpc> createContract(SoliditySource soliditySrc, String contractName, EthAccount sender, Object... constructorArgs) throws IOException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(EthValue.wei(1), EthData.of(ByteUtil.merge(Hex.decode(metadata.bin), argsEncoded)), sender)
                .thenApply(address -> new SmartContractRpc(metadata.abi, web3JFacade, sender, address, this));
    }

    private CompilationResult.ContractMetadata compile(SoliditySource src, String contractName) throws IOException {
        SolidityCompiler.Result result = SolidityCompiler.compile(src.getSource().getBytes(EthereumFacade.CHARSET), true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (result.isFailed()) {
            throw new EthereumApiException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.contracts.isEmpty()) {
            throw new EthereumApiException("Compilation failed, no contracts returned:\n" + result.errors);
        }
        CompilationResult.ContractMetadata metadata = res.contracts.get(contractName);
        if (metadata != null && (metadata.bin == null || metadata.bin.isEmpty())) {
            throw new EthereumApiException("Compilation failed, no binary returned:\n" + result.errors);
        }
        return metadata;
    }

    private CompletableFuture<TransactionReceipt> waitForTransactionReceipt(EthData transactionHash) {
        return CompletableFuture.supplyAsync(() -> getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS)
                .<EthereumApiException>orElseThrow(() -> new EthereumApiException("Transaction reciept not generated after " + ATTEMPTS + " attempts")));
    }

    private Optional<TransactionReceipt> getTransactionReceipt(EthData transactionHash, int sleepDuration, int attempts) {
        Optional<TransactionReceipt> receiptOptional =
                sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    throw new EthereumApiException("error while waiting for the transaction receipt", e);
                }
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }
        return receiptOptional;
    }

    private Optional<TransactionReceipt> sendTransactionReceiptRequest(EthData transactionHash) {
        return Optional.ofNullable(web3JFacade.getTransactionReceipt(transactionHash));
    }

    public CompletableFuture<EthExecutionResult> sendTx(final EthValue value, final EthData data, final EthAccount sender, final EthAddress toAddress) {
        BigInteger nonce = web3JFacade.getTransactionCount(sender);
        BigInteger gas = web3JFacade.estimateGas(sender, data);
        BigInteger gasPrice = web3JFacade.getGasPrice();

        increasePendingTransactionCounter(sender);

        org.ethereum.core.Transaction tx = new org.ethereum.core.Transaction(
                ByteUtil.bigIntegerToBytes(getNonce(sender, nonce)),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice.longValue()),
                ByteUtil.longToBytesNoLeadZeroes(gas.longValue()),
                Optional.ofNullable(toAddress).map(addr -> addr.address).orElse(null),
                ByteUtil.longToBytesNoLeadZeroes(value.inWei().longValue()),
                data.data,
                chainId.id);
        tx.sign(sender.key);

        return CompletableFuture.supplyAsync(() -> {
            web3JFacade.sendTransaction(EthData.of(tx.getEncoded()));
            decreasePendingTransactionCounter(sender);
            return new EthExecutionResult(new byte[0]);
        });
    }

    private BigInteger getNonce(final EthAccount account, final BigInteger nonce) {
        return nonce.add(pendingTransactions.getOrDefault(account, BigInteger.ZERO)).subtract(BigInteger.ONE);
    }

    public CompletableFuture<EthAddress> sendTx(final EthValue ethValue, final EthData data, final EthAccount sender) {
        BigInteger nonce = web3JFacade.getTransactionCount(sender);
        BigInteger gas = web3JFacade.estimateGas(sender, data);
        BigInteger gasPrice = web3JFacade.getGasPrice();
        increasePendingTransactionCounter(sender);

        RawTransaction tx = RawTransaction.createContractTransaction(
                getNonce(sender, nonce),
                gasPrice,
                gas.add(BigInteger.valueOf(100_000)),
                ethValue.inWei(),
                data.toString());
        EthData signedTx = EthData.of(TransactionEncoder.signMessage(tx, sender.credentials));
        EthData result = web3JFacade.sendTransaction(signedTx);
        return this.handleTransaction(result)
                .thenApply(receipt -> {
                    decreasePendingTransactionCounter(sender);
                    return EthAddress.of(receipt.getContractAddress().orElse(null));
                });
    }

    private CompletableFuture<TransactionReceipt> handleTransaction(final EthData result) {

        log.info("transaction " + result.toString() + " has been sent. Waiting to be mined");
        return waitForTransactionReceipt(result);
    }

    @Override
    public EthereumEventHandler events() {
        throw new EthereumApiException("event handling is not yet implemented for RPC");
    }

    @Override
    public boolean addressExists(EthAddress address) {
        throw new EthereumApiException("addressExists is not implemented for RPC");
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        EthGetBalance result = web3JFacade.getBalance(address);
        if (result.hasError()) {
            throw new EthereumApiException(result.getError().getMessage());
        }
        return EthValue.wei(result.getBalance());
    }

    private void decreasePendingTransactionCounter(EthAccount sender) {
        pendingTransactions.put(sender, pendingTransactions.getOrDefault(sender, BigInteger.ZERO).subtract(BigInteger.ONE));
    }

    private void increasePendingTransactionCounter(EthAccount sender) {
        pendingTransactions.put(sender, pendingTransactions.getOrDefault(sender, BigInteger.ZERO).add(BigInteger.ONE));
    }
}
