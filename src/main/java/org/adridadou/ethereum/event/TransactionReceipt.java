package org.adridadou.ethereum.event;

import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthHash;

/**
 * Created by davidroon on 03.02.17.
 */
public class TransactionReceipt {
    public final EthHash hash;
    public final EthAddress sender;
    public final EthAddress receiveAddress;
    public final String error;
    public final EthData executionResult;
    public final boolean isSuccessful;

    public TransactionReceipt(EthHash hash, EthAddress sender, EthAddress receiveAddress, String error, EthData executionResult, boolean isSuccessful) {
        this.hash = hash;
        this.sender = sender;
        this.receiveAddress = receiveAddress;
        this.error = error;
        this.executionResult = executionResult;
        this.isSuccessful = isSuccessful;
    }

    @Override
    public String toString() {
        return "TransactionReceipt{" +
                "hash=" + hash +
                ", sender=" + sender +
                ", receiveAddress=" + receiveAddress +
                ", error='" + error + '\'' +
                ", executionResult=" + executionResult +
                ", isSuccessful=" + isSuccessful +
                '}';
    }
}
