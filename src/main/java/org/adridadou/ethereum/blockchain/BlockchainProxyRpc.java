package org.adridadou.ethereum.blockchain;

import static org.adridadou.ethereum.values.EthValue.wei;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractRpc;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.ContractAbi;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthExecutionResult;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.ethereum.values.SmartContractByteCode;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import rx.Observable;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyRpc implements BlockchainProxy {

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
        return createContract(contract, sender, constructorArgs);
    }

    private CompletableFuture<EthAddress> createContract(CompiledContract compiledContract, EthAccount sender, Object... constructorArgs) {
        CallTransaction.Contract contract = new CallTransaction.Contract(compiledContract.getAbi().getAbi());
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(wei(0), EthData.of(ByteUtil.merge(compiledContract.getBinary().data, argsEncoded)), sender);
    }

    private CompletableFuture<TransactionReceipt> waitForTransactionReceipt(EthData transactionHash) {
        return CompletableFuture.supplyAsync(() -> getTransactionReceipt(transactionHash)
                .<EthereumApiException>orElseThrow(() -> new EthereumApiException("Transaction receipt not found!")));
    }

    private Optional<TransactionReceipt> getTransactionReceipt(EthData transactionHash) {
        web3JFacade.observeTransactionsFromBlock()
                .filter(tx -> EthData.of(tx.getHash()).equals(transactionHash))
                .toBlocking().first();
        return Optional.ofNullable(web3JFacade.getTransactionReceipt(transactionHash));
    }

    @Override
    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        BigInteger gasPrice = web3JFacade.getGasPrice();
        BigInteger gasLimit = web3JFacade.estimateGas(account, data);

        increasePendingTransactionCounter(account.getAddress());

        org.ethereum.core.Transaction tx = new org.ethereum.core.Transaction(
                ByteUtil.bigIntegerToBytes(getNonce(account.getAddress())),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice.longValue()),
                ByteUtil.longToBytesNoLeadZeroes(gasLimit.longValue()),
                Optional.ofNullable(toAddress).map(addr -> addr.address).orElse(null),
                ByteUtil.longToBytesNoLeadZeroes(value.inWei().longValue()),
                data.data,
                (byte) chainId.id);
        tx.sign(account.key);

        return CompletableFuture.supplyAsync(() -> {
            web3JFacade.sendTransaction(EthData.of(tx.getEncoded()));
            decreasePendingTransactionCounter(account.getAddress());
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
    public <T> Observable<T> observeEvents(ContractAbi abi, EthAddress contractAddress, String eventName, Class<T> cls) {
        return web3JFacade.event(contractAddress, eventName, new CallTransaction.Contract(abi.getAbi()), cls);
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
