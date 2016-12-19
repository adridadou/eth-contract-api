package org.adridadou.ethereum.blockchain;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContractReal;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import static org.adridadou.ethereum.values.EthValue.wei;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyReal implements BlockchainProxy {

    private static final long BLOCK_WAIT_LIMIT = 16;
    private final Ethereum ethereum;
    private final EthereumEventHandler eventHandler;
    private final Map<EthAddress, BigInteger> pendingTransactions = new ConcurrentHashMap<>();

    public BlockchainProxyReal(Ethereum ethereum, EthereumEventHandler eventHandler) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        eventHandler.onReady().thenAccept((b) -> ethereum.getBlockchain().flush());
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
        return new SmartContractReal(abi.getAbi(), ethereum, sender, address, this);
    }

    @Override
    public CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs) {
        try {
            return createContract(code, contractName, sender, constructorArgs).thenApply(SmartContractReal::getAddress);
        } catch (IOException e) {
            throw new EthereumApiException("error while publishing " + contractName + ":", e);
        }
    }

    private CompletableFuture<SmartContractReal> createContract(SoliditySource soliditySrc, String contractName, EthAccount sender, Object... constructorArgs) throws IOException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(wei(0), EthData.of(ByteUtil.merge(Hex.decode(metadata.bin), argsEncoded)), sender)
                .thenApply(address -> new SmartContractReal(metadata.abi, ethereum, sender, address, this));
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

    public BigInteger getNonce(final EthAddress address) {
        BigInteger nonce = ((BlockchainImpl) ethereum.getBlockchain()).getRepository().getNonce(address.address);
        return nonce.add(pendingTransactions.getOrDefault(address, BigInteger.ZERO));
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        byte[] code = ((BlockchainImpl) ethereum.getBlockchain()).getRepository().getCode(address.address);

        return SmartContractByteCode.of(code);
    }

    @Override
    public CompletableFuture<EthAddress> sendTx(EthValue ethValue, EthData data, EthAccount sender) {
        return this.sendTxInternal(ethValue, data, sender, EthAddress.empty())
                .thenApply(receipt -> EthAddress.of(receipt.getTransaction().getContractAddress()));
    }

    @Override
    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address) {
        return this.sendTxInternal(value, data, sender, address)
                .thenApply(receipt -> new EthExecutionResult(receipt.getExecutionResult()));
    }

    private CompletableFuture<TransactionReceipt> sendTxInternal(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        return eventHandler.onReady().thenCompose((b) -> {
            BigInteger nonce = getNonce(account.getAddress());
            Transaction tx = new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                    ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                    toAddress.address,
                    ByteUtil.longToBytesNoLeadZeroes(value.inWei().longValue()),
                    data.data,
                    ethereum.getChainIdForNextBlock());
            tx.sign(account.key);
            ethereum.submitTransaction(tx);
            increasePendingTransactionCounter(account.getAddress());
            long currentBlock = eventHandler.getCurrentBlockNumber();

            Predicate<TransactionReceipt> findReceipt = (TransactionReceipt receipt) -> new ByteArrayWrapper(receipt.getTransaction().getHash()).equals(new ByteArrayWrapper(tx.getHash()));

            return CompletableFuture.supplyAsync(() -> eventHandler.observeBlocks()
                    .filter(params -> params.receipts.stream().anyMatch(findReceipt) || params.block.getNumber() > currentBlock + BLOCK_WAIT_LIMIT)
                    .map(params -> {
                        Optional<TransactionReceipt> receipt = params.receipts.stream().filter(findReceipt).findFirst();
                        decreasePendingTransactionCounter(account.getAddress());
                        return receipt.map(eventHandler::checkForErrors)
                                .<EthereumApiException>orElseThrow(() -> new EthereumApiException("the transaction has not been added to any block after waiting for " + BLOCK_WAIT_LIMIT));
                    }).toBlocking().first());
        });
    }

    @Override
    public EthereumEventHandler events() {
        return eventHandler;
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return ethereum.getRepository().isExist(address.address);
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return wei(ethereum.getRepository().getBalance(address.address));
    }

    private void decreasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).subtract(BigInteger.ONE));
    }

    private void increasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).add(BigInteger.ONE));
    }
}
