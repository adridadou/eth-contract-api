package org.adridadou.ethereum.event;

import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthHash;

/**
 * Created by davidroon on 03.02.17.
 * This code is released under Apache 2 license
 */
public class TransactionReceipt {
    public final EthHash hash;
    public final EthAddress sender;
    public final EthAddress receiveAddress;
    public final EthAddress contractAddress;
    public final String error;
    public final EthData executionResult;
    public final boolean isSuccessful;

    public TransactionReceipt(EthHash hash, EthAddress sender, EthAddress receiveAddress, EthAddress contractAddress, String error, EthData executionResult, boolean isSuccessful) {
        this.hash = hash;
        this.sender = sender;
        this.receiveAddress = receiveAddress;
        this.contractAddress = contractAddress;
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
                ", contractAddress=" + contractAddress +
                ", error='" + error + '\'' +
                ", executionResult=" + executionResult +
                ", isSuccessful=" + isSuccessful +
                '}';
    }
}
