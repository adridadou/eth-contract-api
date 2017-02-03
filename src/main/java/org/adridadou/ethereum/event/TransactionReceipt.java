package org.adridadou.ethereum.event;

import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;

/**
 * Created by davidroon on 03.02.17.
 */
public class TransactionReceipt {
    public final EthData hash;
    public final EthAddress sender;
    public final EthAddress receiveAddress;
    public final String error;
    public final EthData executionResult;
    public final boolean isSuccessful;

    public TransactionReceipt(EthData hash, EthAddress sender, EthAddress receiveAddress, String error, EthData executionResult, boolean isSuccessful) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionReceipt that = (TransactionReceipt) o;

        if (isSuccessful != that.isSuccessful) return false;
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        if (receiveAddress != null ? !receiveAddress.equals(that.receiveAddress) : that.receiveAddress != null)
            return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        return executionResult != null ? executionResult.equals(that.executionResult) : that.executionResult == null;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (receiveAddress != null ? receiveAddress.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (executionResult != null ? executionResult.hashCode() : 0);
        result = 31 * result + (isSuccessful ? 1 : 0);
        return result;
    }
}
