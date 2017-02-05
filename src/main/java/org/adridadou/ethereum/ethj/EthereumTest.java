package org.adridadou.ethereum.ethj;

import org.adridadou.ethereum.EthereumBackend;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumTest implements EthereumBackend {
    private final StandaloneBlockchain blockchain;
    private final TestConfig testConfig;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);
    private final LocalExecutionService localExecutionService;

    public EthereumTest(TestConfig testConfig) {
        SystemProperties.getDefault().setBlockchainConfig(new HomesteadConfig(new HomesteadConfig.HomesteadConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        this.blockchain = new StandaloneBlockchain();

        blockchain
                .withGasLimit(testConfig.getGasLimit())
                .withGasPrice(testConfig.getGasPrice())
                .withCurrentTime(testConfig.getInitialTime());

        testConfig.getBalances().entrySet()
                .forEach(entry -> blockchain.withAccountBalance(entry.getKey().getAddress().address, entry.getValue().inWei()));

        localExecutionService = new LocalExecutionService(blockchain.getBlockchain());
        CompletableFuture.runAsync(() -> {
            try {
                while(true) {
                    blockchain.submitTransaction(transactions.take());
                    blockchain.createBlock();
                }
            } catch (InterruptedException e) {
                throw new EthereumApiException("error while polling transactions for test env", e);
            }
        });

        this.testConfig = testConfig;
    }

    public EthAccount defaultAccount() {
        return AccountProvider.from(this.blockchain.getSender());
    }

    @Override
    public BigInteger getGasPrice() {
        return BigInteger.valueOf(testConfig.getGasPrice());
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return EthValue.wei(blockchain.getBlockchain().getRepository().getBalance(address.address));
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return blockchain.getBlockchain().getRepository().isExist(address.address);
    }

    @Override
    public EthData submit(EthAccount account, EthAddress address, EthValue value, EthData data, BigInteger nonce) {
        Transaction tx = createTransaction(account, nonce, address, value, data);
        transactions.add(tx);

        return EthData.of(tx.getHash());
    }

    private Transaction createTransaction(EthAccount account, BigInteger nonce, EthAddress address, EthValue value, EthData data) {
        return blockchain.createTransaction(account.key, nonce.longValue(),address.address, value.inWei(), data.data);
    }

    @Override
    public BigInteger estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        Transaction tx = createTransaction(account, getNonce(account.getAddress()), address, value, data);
        Block callBlock = blockchain.getBlockchain().getBestBlock();
        Repository repository = blockchain.getBlockchain().getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();
        try {
            TransactionExecutor executor = new TransactionExecutor
                    (tx, callBlock.getCoinbase(), repository, blockchain.getBlockchain().getBlockStore(),
                    blockchain.getBlockchain().getProgramInvokeFactory(), callBlock)
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

    @Override
    public BigInteger getNonce(EthAddress currentAddress) {
        return blockchain.getBlockchain().getRepository().getNonce(currentAddress.address);
    }

    @Override
    public long getCurrentBlockNumber() {
        return blockchain.getBlockchain().getBestBlock().getNumber();
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return SmartContractByteCode.of(blockchain.getBlockchain().getRepository().getCode(address.address));
    }

    @Override
    public EthData executeLocally(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        return localExecutionService.executeLocally(account, address,value, data, getNonce(account.getAddress()));
    }

    @Override
    public void register(EthereumEventHandler eventHandler) {
        eventHandler.onReady();
        blockchain.addEthereumListener(new EthJEventListener(eventHandler));
    }
}
