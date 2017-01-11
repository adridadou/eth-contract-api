package org.adridadou.ethereum.event;

import java.util.List;
import java.util.Optional;

import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.vm.LogInfo;

public class OnTransactionParameters {

    public final TransactionReceipt receipt;
    public final EthData txHash;
    public final TransactionStatus status;
    public final String error;
    public final List<LogInfo> logs;
    public final EthAddress sender;
    public final EthAddress receiver;
    public final Boolean isContractCreation;


    public OnTransactionParameters(TransactionReceipt receipt, EthData txHash, TransactionStatus status, String error, List<LogInfo> logs, byte[] sender, byte[] receiver) {
        this.receipt = receipt;
        this.txHash = txHash;
        this.status = status;
        this.error = error;
        this.logs = logs;
        this.sender = EthAddress.of(sender);
        this.receiver = Optional.ofNullable(receiver).map(bytes ->  EthAddress.of(bytes)).orElse(null);
        this.isContractCreation = this.receiver == null || this.receiver.isEmpty();
    }

    @Override
    public String toString() {
        return "OnTransactionParameters{" +
                "receipt=" + receipt +
                ", txHash=" + txHash +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", logs=" + logs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnTransactionParameters that = (OnTransactionParameters) o;

        if (receipt != null ? !receipt.equals(that.receipt) : that.receipt != null) return false;
        if (txHash != null ? !txHash.equals(that.txHash) : that.txHash != null) return false;
        if (status != that.status) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        return logs != null ? logs.equals(that.logs) : that.logs == null;
    }

    @Override
    public int hashCode() {
        int result = receipt != null ? receipt.hashCode() : 0;
        result = 31 * result + (txHash != null ? txHash.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (logs != null ? logs.hashCode() : 0);
        return result;
    }
}
