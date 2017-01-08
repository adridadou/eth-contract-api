package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractRpc;
import org.adridadou.ethereum.values.*;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.*;
import rx.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.adridadou.ethereum.values.EthValue.wei;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyRpc implements BlockchainProxy {

    private static final int SLEEP_DURATION = 5000;
    private static final int ATTEMPTS = 120;
    private static final Logger log = LoggerFactory.getLogger(BlockchainProxyRpc.class);
    private final Map<EthAddress, BigInteger> pendingTransactions = new CopyOnWriteMap<>();
    private final ChainId chainId;

    private final Web3JFacade web3JFacade;

    public BlockchainProxyRpc(Web3JFacade web3jFacade, ChainId chainId) {
        this.web3JFacade = web3jFacade;
        this.chainId = chainId;
    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractRpc(abi.getAbi(), web3JFacade, sender, address, this);
    }

    @Override
    public CompletableFuture<EthAddress> publish(CompiledContract contract, EthAccount sender, Object... constructorArgs) {
        try {
            return createContract(contract, sender, constructorArgs);
        } catch (IOException e) {
            throw new EthereumApiException("error while publishing the contract", e);
        }
    }

    private CompletableFuture<EthAddress> createContract(CompiledContract compiledContract, EthAccount sender, Object... constructorArgs) throws IOException {
        CallTransaction.Contract contract = new CallTransaction.Contract(compiledContract.getAbi().getAbi());
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(wei(0), EthData.of(ByteUtil.merge(compiledContract.getBinary().data, argsEncoded)), sender);
    }

    private CompletableFuture<TransactionReceipt> waitForTransactionReceipt(EthData transactionHash) {
        return CompletableFuture.supplyAsync(() -> getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS)
                .orElseThrow(() -> new EthereumApiException("Transaction reciept not generated after " + ATTEMPTS + " attempts")));
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

    @Override
    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress toAddress) {
        BigInteger gasPrice = web3JFacade.getGasPrice();
        BigInteger gasLimit = web3JFacade.estimateGas(sender, data);

        increasePendingTransactionCounter(sender.getAddress());

        org.ethereum.core.Transaction tx = new org.ethereum.core.Transaction(
                ByteUtil.bigIntegerToBytes(getNonce(sender.getAddress())),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice.longValue()),
                ByteUtil.longToBytesNoLeadZeroes(gasLimit.longValue()),
                Optional.ofNullable(toAddress).map(addr -> addr.address).orElse(null),
                ByteUtil.longToBytesNoLeadZeroes(value.inWei().longValue()),
                data.data,
                (byte) chainId.id);
        tx.sign(sender.key);

        return CompletableFuture.supplyAsync(() -> {
            web3JFacade.sendTransaction(EthData.of(tx.getEncoded()));
            decreasePendingTransactionCounter(sender.getAddress());
            return new EthExecutionResult(new byte[0]);
        });
    }

    public BigInteger getNonce(EthAddress address) {
        return web3JFacade.getTransactionCount(address)
                .add(pendingTransactions.getOrDefault(address, BigInteger.ZERO))
                .subtract(BigInteger.ONE);
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return web3JFacade.getCode(address);
    }

    @Override
    public <T> Observable<T> observeEvents(EthAddress contractAddress, String eventName, Class<T> cls) {

        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void shutdown() {
        //do nothing
    }

    @Override
    public CompletableFuture<EthAddress> sendTx(EthValue ethValue, EthData data, EthAccount sender) {
        BigInteger gasPrice = web3JFacade.getGasPrice();
        BigInteger gasLimit = web3JFacade.estimateGas(sender, data);

        increasePendingTransactionCounter(sender.getAddress());

        RawTransaction tx = RawTransaction.createContractTransaction(
                getNonce(sender.getAddress()),
                gasPrice,
                gasLimit,
                ethValue.inWei(),
                data.toString());
        EthData signedTx = EthData.of(TransactionEncoder.signMessage(tx, sender.credentials));
        EthData result = web3JFacade.sendTransaction(signedTx);
        return this.handleTransaction(result)
                .thenApply(receipt -> {
                    decreasePendingTransactionCounter(sender.getAddress());
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
        return wei(result.getBalance());
    }

    private void decreasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).subtract(BigInteger.ONE));
    }

    private void increasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).add(BigInteger.ONE));
    }
}
