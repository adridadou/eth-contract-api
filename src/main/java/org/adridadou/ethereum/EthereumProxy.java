    package org.adridadou.ethereum;

    import static org.adridadou.ethereum.values.EthValue.wei;

    import java.math.BigInteger;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Map;
    import java.util.Optional;
    import java.util.concurrent.CompletableFuture;
    import java.util.concurrent.ConcurrentHashMap;

    import org.adridadou.ethereum.converters.input.InputTypeHandler;
    import org.adridadou.ethereum.converters.output.OutputTypeHandler;
    import org.adridadou.ethereum.event.*;
    import org.adridadou.ethereum.values.CompiledContract;
    import org.adridadou.ethereum.values.ContractAbi;
    import org.adridadou.ethereum.values.EthAccount;
    import org.adridadou.ethereum.values.EthAddress;
    import org.adridadou.ethereum.values.EthData;
    import org.adridadou.ethereum.values.EthExecutionResult;
    import org.adridadou.ethereum.values.EthValue;
    import org.adridadou.ethereum.values.SmartContractByteCode;
    import org.adridadou.exception.EthereumApiException;
    import org.ethereum.core.CallTransaction;
    import org.ethereum.core.TransactionReceipt;
    import org.ethereum.util.ByteUtil;
    import rx.Observable;

    /**
    * Created by davidroon on 20.04.16.
    * This code is released under Apache 2 license
    */
    public class EthereumProxy {
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
                    .filter(params -> params.receiver.equals(contractAddress))
                    .flatMap(params -> Observable.from(params.logs))
                    .map(contract::parseEvent)
                    .filter(invocation -> invocation != null && eventName.equals(invocation.function.name))
                    .map(invocation -> outputTypeHandler.convertSpecificType(invocation.args, cls));
    }

    public CompletableFuture<EthAddress> publishContract(EthValue ethValue, EthData data, EthAccount account) {
        return this.sendTxInternal(ethValue, data, account, EthAddress.empty())
                .thenApply(receipt -> EthAddress.of(receipt.getTransaction().getContractAddress()));
    }

    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount account, EthAddress address) {
        return this.sendTxInternal(value, data, account, address)
                .thenApply(receipt -> new EthExecutionResult(receipt.getExecutionResult()));
    }

    private CompletableFuture<TransactionReceipt> sendTxInternal(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        return eventHandler.onReady().thenCompose((b) -> {
            EthData txHash = ethereum.submit(account, toAddress,value,data, getNonce(account.getAddress()));

            long currentBlock = eventHandler.getCurrentBlockNumber();

            CompletableFuture<TransactionReceipt> result = CompletableFuture.supplyAsync(() -> {
                Observable<OnTransactionParameters> droppedTxs = eventHandler.observeTransactions().filter(params -> Arrays.equals(params.txHash.data, txHash.data) && params.status == TransactionStatus.Dropped);
                Observable<OnTransactionParameters> timeoutBlock = eventHandler.observeBlocks().filter(blockParams -> blockParams.block.getNumber() > currentBlock + BLOCK_WAIT_LIMIT).map(params -> null);
                Observable<OnTransactionParameters> blockTxs = eventHandler.observeBlocks()
                        .flatMap(params -> Observable.from(params.receipts))
                        .filter(receipt -> Arrays.equals(receipt.getTransaction().getHash(), txHash.data))
                        .map(receipt -> new OnTransactionParameters(receipt, EthData.of(receipt.getTransaction().getHash()), TransactionStatus.Executed, receipt.getError(), new ArrayList<>(), receipt.getTransaction().getSender(), receipt.getTransaction().getReceiveAddress()));

                return Observable.merge(droppedTxs, blockTxs, timeoutBlock)
                        .map(params -> {
                            if (params == null) {
                                throw new EthereumApiException("the transaction has not been included in the last " + BLOCK_WAIT_LIMIT + " blocks");
                            }
                            TransactionReceipt receipt = params.receipt;
                            if (params.status == TransactionStatus.Dropped) {
                                throw new EthereumApiException("the transaction has been dropped! - " + params.error);
                            }
                            return eventHandler.checkForErrors(receipt);
                        }).toBlocking().first();

            });
            increasePendingTransactionCounter(account.getAddress());
            return result;
        });
    }

    private void updateNonce() {
        eventHandler.observeTransactions()
                .filter(tx -> tx.status == TransactionStatus.Dropped)
                .forEach(params -> {
            EthAddress currentAddress = EthAddress.of(params.receipt.getTransaction().getSender());
            Optional.ofNullable(pendingTransactions.get(currentAddress)).ifPresent(counter -> {
                pendingTransactions.put(currentAddress, counter.subtract(BigInteger.ONE));
                nonces.put(currentAddress, ethereum.getNonce(currentAddress));
            });
        });
        eventHandler.observeBlocks()
            .forEach(params -> params.block.getTransactionsList().stream()
                .map(tx -> EthAddress.of(tx.getSender()))
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
