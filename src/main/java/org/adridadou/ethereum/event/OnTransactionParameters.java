package org.adridadou.ethereum.event;

import java.util.List;
import org.ethereum.vm.LogInfo;

public class OnTransactionParameters {

    public final TransactionReceipt receipt;
    public final TransactionStatus status;
    public final List<LogInfo> logs;
    public final Boolean isContractCreation;


    public OnTransactionParameters(TransactionReceipt receipt, TransactionStatus status, List<LogInfo> logs) {
        this.receipt = receipt;
        this.status = status;
        this.logs = logs;
        this.isContractCreation = this.receipt != null && this.receipt.receiveAddress.isEmpty();
    }

    @Override
    public String toString() {
        return "OnTransactionParameters{" +
                "receipt=" + receipt +
                ", status=" + status +
                ", logs=" + logs +
                ", isContractCreation=" + isContractCreation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnTransactionParameters that = (OnTransactionParameters) o;

        if (receipt != null ? !receipt.equals(that.receipt) : that.receipt != null) return false;
        if (status != that.status) return false;
        if (logs != null ? !logs.equals(that.logs) : that.logs != null) return false;
        return isContractCreation != null ? isContractCreation.equals(that.isContractCreation) : that.isContractCreation == null;
    }

    @Override
    public int hashCode() {
        int result = receipt != null ? receipt.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (logs != null ? logs.hashCode() : 0);
        result = 31 * result + (isContractCreation != null ? isContractCreation.hashCode() : 0);
        return result;
    }
}
