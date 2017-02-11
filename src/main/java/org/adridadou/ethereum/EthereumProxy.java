package org.adridadou.ethereum;

import static org.adridadou.ethereum.values.EthValue.wei;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.*;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.util.ByteUtil;
import rx.Observable;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumProxy {
    public static final int ADDITIONAL_GAS_FOR_CONTRACT_CREATION = 15_000;
    public static final int ADDITIONAL_GAS_DIRTY_FIX = 200_000;
    private static final long BLOCK_WAIT_LIMIT = 16;

    private final EthereumBackend ethereum;
    private final EthereumEventHandler eventHandler;
    private final Map<EthAddress, BigInteger> pendingTransactions = new ConcurrentHashMap<>();
    private final Map<EthAddress, BigInteger> nonces = new ConcurrentHashMap<>();
    private final InputTypeHandler inputTypeHandler;
    private final OutputTypeHandler outputTypeHandler;

    public EthereumProxy(EthereumBackend ethereum, EthereumEventHandler eventHandler, InputTypeHandler inputTypeHandler, OutputTypeHandler outputTypeHandler) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        this.inputTypeHandler = inputTypeHandler;
        this.outputTypeHandler = outputTypeHandler;
        updateNonce();
        ethereum.register(eventHandler);
    }

    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount account) {
        return new SmartContract(new CallTransaction.Contract(abi.getAbi()), account, address, this, ethereum);
    }

    public CompletableFuture<EthAddress> publish(CompiledContract contract, EthAccount account, Object... constructorArgs) {
        return createContract(contract, account, constructorArgs);
    }

    private CompletableFuture<EthAddress> createContract(CompiledContract contract, EthAccount account, Object... constructorArgs) {
        CallTransaction.Contract contractAbi = new CallTransaction.Contract(contract.getAbi().getAbi());
        CallTransaction.Function constructor = contractAbi.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(prepareArguments(constructorArgs));
        return publishContract(wei(0), EthData.of(ByteUtil.merge(contract.getBinary().data, argsEncoded)), account);
    }

    public Object[] prepareArguments(Object[] args) {
        return Arrays.stream(args)
                .map(inputTypeHandler::convert)
                .toArray();
    }

    public BigInteger getNonce(final EthAddress address) {
        nonces.computeIfAbsent(address, ethereum::getNonce);
        BigInteger nonce = nonces.get(address);
        return nonce.add(pendingTransactions.getOrDefault(address, BigInteger.ZERO));
    }

    public SmartContractByteCode getCode(EthAddress address) {
        return ethereum.getCode(address);
    }

    public <T> Observable<T> observeEvents(ContractAbi abi, EthAddress contractAddress, String eventName, Class<T> cls) {
        CallTransaction.Contract contract = new CallTransaction.Contract(abi.getAbi());
        return eventHandler.observeTransactions()
                .filter(params -> contractAddress.equals(params.receipt.receiveAddress))
                .flatMap(params -> Observable.from(params.logs))
                .map(contract::parseEvent)
                .filter(invocation -> invocation != null && eventName.equals(invocation.function.name))
                .map(invocation -> outputTypeHandler.convertSpecificType(invocation.args, cls));
    }

    public CompletableFuture<EthAddress> publishContract(EthValue ethValue, EthData data, EthAccount account) {
        return this.sendTxInternal(ethValue, data, account, EthAddress.empty())
                .thenApply(receipt -> receipt.contractAddress);
    }

    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount account, EthAddress address) {
        return this.sendTxInternal(value, data, account, address)
                .thenApply(receipt -> new EthExecutionResult(receipt.executionResult));
    }

    private CompletableFuture<TransactionReceipt> sendTxInternal(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        return eventHandler.ready().thenCompose((v) -> {
            BigInteger gasLimit = estimateGas(value, data, account, toAddress);
            EthHash txHash = ethereum.submit(account, toAddress, value, data, getNonce(account.getAddress()), gasLimit);

            long currentBlock = eventHandler.getCurrentBlockNumber();

            CompletableFuture<TransactionReceipt> result = CompletableFuture.supplyAsync(() -> {
                Observable<OnTransactionParameters> droppedTxs = eventHandler.observeTransactions()
                        .filter(params -> params.receipt != null && Objects.equals(params.receipt.hash, txHash) && params.status == TransactionStatus.Dropped);
                Observable<OnTransactionParameters> timeoutBlock = eventHandler.observeBlocks()
                        .filter(blockParams -> blockParams.blockNumber > currentBlock + BLOCK_WAIT_LIMIT)
                        .map(params -> null);
                Observable<OnTransactionParameters> blockTxs = eventHandler.observeBlocks()
                        .flatMap(params -> Observable.from(params.receipts))
                        .filter(receipt -> Objects.equals(receipt.hash, txHash))
                        .map(this::createTransactionParameters);

                return Observable.merge(droppedTxs, blockTxs, timeoutBlock)
                        .map(params -> {
                            if (params == null) {
                                throw new EthereumApiException("the transaction has not been included in the last " + BLOCK_WAIT_LIMIT + " blocks");
                            }
                            TransactionReceipt receipt = params.receipt;
                            if (params.status == TransactionStatus.Dropped) {
                                throw new EthereumApiException("the transaction has been dropped! - " + params.receipt.error);
                            }
                            return checkForErrors(receipt);
                        }).toBlocking().first();

            });
            increasePendingTransactionCounter(account.getAddress());
            return result;
        });
    }

    private BigInteger estimateGas(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        BigInteger gasLimit = ethereum.estimateGas(account, toAddress, value, data);
        //if it is a contract creation
        if (toAddress.isEmpty()) {
            gasLimit = gasLimit.add(BigInteger.valueOf(ADDITIONAL_GAS_FOR_CONTRACT_CREATION));
        }
        return gasLimit.add(BigInteger.valueOf(ADDITIONAL_GAS_DIRTY_FIX));
    }

    private OnTransactionParameters createTransactionParameters(TransactionReceipt receipt) {
        return new OnTransactionParameters(receipt, TransactionStatus.Executed, new ArrayList<>());
    }

    private TransactionReceipt checkForErrors(final TransactionReceipt receipt) {
        if (receipt.isSuccessful) {
            return receipt;
        } else {
            throw new EthereumApiException("error with the transaction " + receipt.hash + ". error:" + receipt.error);
        }
    }

    private void updateNonce() {
        eventHandler.observeTransactions()
                .filter(tx -> tx.status == TransactionStatus.Dropped)
                .forEach(params -> {
                    EthAddress currentAddress = params.receipt.sender;
                    Optional.ofNullable(pendingTransactions.get(currentAddress)).ifPresent(counter -> {
                        pendingTransactions.put(currentAddress, counter.subtract(BigInteger.ONE));
                        nonces.put(currentAddress, ethereum.getNonce(currentAddress));
                    });
                });
        eventHandler.observeBlocks()
                .forEach(params -> params.receipts.stream()
                        .map(tx -> tx.sender)
                        .forEach(currentAddress -> Optional.ofNullable(pendingTransactions.get(currentAddress))
                                .ifPresent(counter -> {
                                    pendingTransactions.put(currentAddress, counter.subtract(BigInteger.ONE));
                                    nonces.put(currentAddress, ethereum.getNonce(currentAddress));
                                })));
    }

    public EthereumEventHandler events() {
        return eventHandler;
    }

    public boolean addressExists(EthAddress address) {
        return ethereum.addressExists(address);
    }

    public EthValue getBalance(EthAddress address) {
        return ethereum.getBalance(address);
    }

    private void increasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).add(BigInteger.ONE));
    }
}
