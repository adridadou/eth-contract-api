package org.adridadou.ethereum.blockchain;

import static org.adridadou.ethereum.values.EthValue.wei;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.*;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractReal;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.ContractAbi;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthExecutionResult;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.ethereum.values.SmartContractByteCode;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.util.ByteUtil;
import rx.Observable;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyReal implements BlockchainProxy {
    public static final BigInteger GAS_LIMIT_FOR_CONSTANT_CALLS = BigInteger.valueOf(100_000_000_000_000L);
    private static final long BLOCK_WAIT_LIMIT = 16;
    private final Ethereumj ethereum;
    private final EthereumEventHandler eventHandler;
    private final Map<EthAddress, BigInteger> pendingTransactions = new ConcurrentHashMap<>();
    private final InputTypeHandler inputTypeHandler;
    private final OutputTypeHandler outputTypeHandler;

    public BlockchainProxyReal(Ethereumj ethereum, EthereumEventHandler eventHandler, InputTypeHandler inputTypeHandler, OutputTypeHandler outputTypeHandler) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        this.inputTypeHandler = inputTypeHandler;
        this.outputTypeHandler = outputTypeHandler;
        eventHandler.onReady().thenAccept((b) -> getBlockchain().flush());
    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractReal(new CallTransaction.Contract(abi.getAbi()), ethereum, sender, address, this);
    }

    @Override
    public CompletableFuture<EthAddress> publish(CompiledContract contract, EthAccount sender, Object... constructorArgs) {
        return createContract(contract, sender, constructorArgs);
    }

    private CompletableFuture<EthAddress> createContract(CompiledContract contract, EthAccount sender, Object... constructorArgs) {
        CallTransaction.Contract contractAbi = new CallTransaction.Contract(contract.getAbi().getAbi());
        CallTransaction.Function constructor = contractAbi.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(prepareArguments(constructorArgs));
        return sendTx(wei(0), EthData.of(ByteUtil.merge(contract.getBinary().data, argsEncoded)), sender);
    }

    public Object[] prepareArguments(Object[] args) {
        return Arrays.stream(args)
                .map(inputTypeHandler::convert)
                .toArray();
    }

    public BigInteger getNonce(final EthAddress address) {
        BigInteger nonce = getBlockchain().getRepository().getNonce(address.address);
        return nonce.add(pendingTransactions.getOrDefault(address, BigInteger.ZERO));
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        byte[] code = getRepository().getCode(address.address);
        if(code.length == 0) {
            throw new EthereumApiException("no code found at the address. please verify that a smart contract is deployed at " + address.withLeading0x());
        }
        return SmartContractByteCode.of(code);
    }

    @Override
    public <T> Observable<T> observeEvents(ContractAbi abi, EthAddress contractAddress, String eventName, Class<T> cls) {
        CallTransaction.Contract contract = new CallTransaction.Contract(abi.getAbi());
        return eventHandler.observeTransactions()
                    .filter(params -> params.receiver.equals(contractAddress))
                    .flatMap(params -> Observable.from(params.logs))
                    .map(contract::parseEvent)
                    .filter(invocation -> invocation != null && eventName.equals(invocation.function.name))
                    .map(invocation -> outputTypeHandler.convertSpecificType(invocation.args, cls));
    }

    @Override
    public void shutdown() {
        ethereum.close();
    }

    @Override
    public CompletableFuture<EthAddress> sendTx(EthValue ethValue, EthData data, EthAccount sender) {
        return this.sendTxInternal(ethValue, data, sender, EthAddress.empty())
                .thenApply(receipt -> EthAddress.of(receipt.getTransaction().getContractAddress()));
    }

    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address) {
        return this.sendTxInternal(value, data, sender, address)
                .thenApply(receipt -> new EthExecutionResult(receipt.getExecutionResult()));
    }

    private CompletableFuture<TransactionReceipt> sendTxInternal(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        return eventHandler.onReady().thenCompose((b) -> {
            BigInteger nonce = getNonce(account.getAddress());
            Transaction txLocal = ethereum.createTransaction(nonce,BigInteger.valueOf(ethereum.getGasPrice()),GAS_LIMIT_FOR_CONSTANT_CALLS, toAddress.address,value.inWei(),data.data);
            txLocal.sign(account.key);

            BigInteger gasLimit = estimateGas(getBlockchain().getBestBlock(), txLocal).add(BigInteger.valueOf(100_000));
            Transaction tx = ethereum.createTransaction(nonce,BigInteger.valueOf(ethereum.getGasPrice()),gasLimit, toAddress.address,value.inWei(),data.data);
            tx.sign(account.key);

            ethereum.submitTransaction(tx);
            increasePendingTransactionCounter(account.getAddress());
            long currentBlock = eventHandler.getCurrentBlockNumber();

            return CompletableFuture.supplyAsync(() -> {
                Observable<OnTransactionParameters> droppedTxs = eventHandler.observeTransactions().filter(params -> Arrays.equals(params.txHash.data, tx.getHash()) && params.status == TransactionStatus.Dropped);
                Observable<OnTransactionParameters> timeoutBlock = eventHandler.observeBlocks().filter(blockParams -> blockParams.block.getNumber() > currentBlock + BLOCK_WAIT_LIMIT).map(params -> null);
                Observable<OnTransactionParameters> blockTxs = eventHandler.observeBlocks()
                        .flatMap(params -> Observable.from(params.receipts))
                        .filter(receipt -> Arrays.equals(receipt.getTransaction().getHash(), tx.getHash()))
                        .map(receipt -> new OnTransactionParameters(receipt, EthData.of(receipt.getTransaction().getHash()),TransactionStatus.Executed,receipt.getError(),new ArrayList<>(), receipt.getTransaction().getSender(), receipt.getTransaction().getReceiveAddress()));

                return Observable.merge(droppedTxs, blockTxs, timeoutBlock)
                        .map(params -> {
                            if(params == null) {
                                throw new EthereumApiException("the transaction has not been included in the last " + BLOCK_WAIT_LIMIT + " blocks");
                            }
                            TransactionReceipt receipt = params.receipt;
                            decreasePendingTransactionCounter(account.getAddress());
                            if(params.status == TransactionStatus.Dropped) {
                                throw new EthereumApiException("the transaction has been dropped! - " + params.error);
                            }
                            return eventHandler.checkForErrors(receipt);
                        }).toBlocking().first();

            });
        });
    }

    @Override
    public EthereumEventHandler events() {
        return eventHandler;
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return getRepository().isExist(address.address);
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return wei(getRepository().getBalance(address.address));
    }

    private void decreasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).subtract(BigInteger.ONE));
    }

    private void increasePendingTransactionCounter(EthAddress address) {
        pendingTransactions.put(address, pendingTransactions.getOrDefault(address, BigInteger.ZERO).add(BigInteger.ONE));
    }

    protected void finalize() {
        ethereum.close();
    }

  private BigInteger estimateGas(Block callBlock, Transaction tx) {
    Repository repository = getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();
    try {
      TransactionExecutor executor = new TransactionExecutor
        (tx, callBlock.getCoinbase(), repository, getBlockchain().getBlockStore(),
          getBlockchain().getProgramInvokeFactory(), callBlock)
        .setLocalCall(true);

      executor.init();
      executor.execute();
      executor.go();
      executor.finalization();
      if(!executor.getReceipt().isSuccessful()) {
          throw new EthereumApiException(executor.getReceipt().getError());
      }
      long gasUsed = executor.getGasUsed();
      return BigInteger.valueOf(gasUsed);
    } finally {
      repository.rollback();
    }
  }

  private BlockchainImpl getBlockchain() {
    return (BlockchainImpl) ethereum.getBlockchain();
  }
  private Repository getRepository() {
    return getBlockchain().getRepository();
  }
}
