package org.adridadou.ethereum.handler;

import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;

public class OnTransactionParameters {

    public final Transaction transaction;
    public final EthereumListener.PendingTransactionState state;

    public OnTransactionParameters(Transaction transaction, EthereumListener.PendingTransactionState state) {
        this.transaction = transaction;
        this.state = state;
    }
}
