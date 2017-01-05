package org.adridadou.ethereum.event;

import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;
import org.ethereum.vm.LogInfo;

import java.util.List;

public class OnTransactionParameters {

    public final Transaction transaction;
    public final EthereumListener.PendingTransactionState state;
    public final List<LogInfo> logs;


    public OnTransactionParameters(Transaction transaction, EthereumListener.PendingTransactionState state, List<LogInfo> logs) {
        this.transaction = transaction;
        this.state = state;
        this.logs = logs;
    }
}
