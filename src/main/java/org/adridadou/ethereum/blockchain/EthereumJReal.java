package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumJReal implements Ethereumj{
    private final Ethereum ethereum;

    public EthereumJReal(Ethereum ethereum) {
        this.ethereum = ethereum;
    }

    @Override
    public Blockchain getBlockchain() {
        return ethereum.getBlockchain();
    }

    @Override
    public void close() {
        ethereum.close();
    }

    @Override
    public long getGasPrice() {
        return ethereum.getGasPrice();
    }

    @Override
    public Future<Void> submitTransaction(Transaction tx) {
        return CompletableFuture.runAsync(() -> {
            try {
                ethereum.submitTransaction(tx).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Transaction createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimitForConstantCalls, byte[] address, BigInteger value, byte[] data) {
        return ethereum.createTransaction(nonce,gasPrice,gasLimitForConstantCalls,address,value, data);
    }

    @Override
    public void addListener(EthereumEventHandler ethereumEventHandler) {
        ethereum.addListener(ethereumEventHandler);
    }
}
