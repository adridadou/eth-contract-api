package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumJTest implements Ethereumj{
    private final StandaloneBlockchain blockchain;
    private final TestConfig config;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);

    public EthereumJTest(TestConfig config) {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        this.blockchain = new StandaloneBlockchain();
        blockchain
                .withGasLimit(config.getGasLimit())
                .withGasPrice(config.getGasPrice())
                .withCurrentTime(config.getInitialTime());

        config.getBalances().entrySet()
                .forEach(entry -> blockchain.withAccountBalance(entry.getKey().getAddress().address, entry.getValue().inWei()));

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

        this.config = config;
    }

    public EthAccount defaultAccount() {
        return AccountProvider.from(this.blockchain.getSender());
    }

    @Override
    public Blockchain getBlockchain() {
        return blockchain.getBlockchain();
    }

    @Override
    public void close() {

    }

    @Override
    public long getGasPrice() {
        return config.getGasPrice();
    }

    @Override
    public void submitTransaction(Transaction tx) {
        transactions.add(tx);
    }

    @Override
    public Transaction createTransaction(EthAccount sender, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimitForConstantCalls, byte[] address, BigInteger value, byte[] data) {
        return blockchain.createTransaction(sender.key, nonce.longValue(),address, value, data);
    }

    @Override
    public void addListener(EthereumEventHandler ethereumEventHandler) {
        blockchain.addEthereumListener(ethereumEventHandler);
    }
}
