package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.values.EthAccount;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumJTest implements Ethereumj{
    private final StandaloneBlockchain blockchain;

    public EthereumJTest() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        this.blockchain = new StandaloneBlockchain();
        blockchain.withAutoblock(true);
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
        return 0;
    }

    @Override
    public void submitTransaction(Transaction tx) {
        blockchain.submitTransaction(tx);
    }

    @Override
    public Transaction createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimitForConstantCalls, byte[] address, BigInteger value, byte[] data) {
        return blockchain.createTransaction(nonce.longValue(),address, value.longValue(), data);
    }

    @Override
    public void addListener(EthereumEventHandler ethereumEventHandler) {
        blockchain.addEthereumListener(ethereumEventHandler);
    }
}
