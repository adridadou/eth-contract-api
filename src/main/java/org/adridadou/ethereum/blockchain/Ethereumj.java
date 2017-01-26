package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;

import java.math.BigInteger;
import java.util.concurrent.Future;

/**
 * Created by davidroon on 20.01.17.
 */
public interface Ethereumj {
    Blockchain getBlockchain();

    void close();

    long getGasPrice();

    void submitTransaction(Transaction tx);

    Transaction createTransaction(BigInteger nonce, BigInteger bigInteger, BigInteger gasLimitForConstantCalls, byte[] address, BigInteger bigInteger1, byte[] data);

    void addListener(EthereumEventHandler ethereumEventHandler);
}
