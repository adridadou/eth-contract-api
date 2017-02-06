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
}
