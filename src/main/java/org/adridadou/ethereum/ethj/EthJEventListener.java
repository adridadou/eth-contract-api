package org.adridadou.ethereum.ethj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.event.OnBlockParameters;
import org.adridadou.ethereum.event.OnTransactionParameters;
import org.adridadou.ethereum.event.TransactionStatus;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthHash;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthJEventListener extends EthereumListenerAdapter {
    private final EthereumEventHandler eventHandler;

    public EthJEventListener(EthereumEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        eventHandler.onBlock(new OnBlockParameters(block.getNumber(), receipts.stream().map(this::toReceipt).collect(Collectors.toList())));
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        TransactionStatus transactionStatus;
        switch(state) {
          case PENDING:
          case NEW_PENDING: transactionStatus = TransactionStatus.Pending; break;
          case DROPPED: transactionStatus = TransactionStatus.Dropped; break;
          case INCLUDED: transactionStatus = TransactionStatus.Included;break;
          default: transactionStatus = TransactionStatus.Unknown;break;
        }
        eventHandler.onPendingTransactionUpdate(new OnTransactionParameters(toReceipt(txReceipt), transactionStatus, new ArrayList<>()));
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {

        OnTransactionParameters mainTransaction = new OnTransactionParameters(toReceipt(summary.getTransaction()), TransactionStatus.Executed, summary.getLogs());
        List<OnTransactionParameters> internalTransactions = summary.getInternalTransactions().stream()
                .map(internalTransaction -> new OnTransactionParameters(toReceipt(internalTransaction), TransactionStatus.Executed, summary.getLogs())).collect(Collectors.toList());

        eventHandler.onTransactionExecuted(mainTransaction, internalTransactions);
    }

    @Override
    public void onSyncDone(final SyncState syncState) {
        eventHandler.onReady();
    }

    private org.adridadou.ethereum.event.TransactionReceipt toReceipt(Transaction tx) {
        return new org.adridadou.ethereum.event.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.empty(), "", EthData.empty(), true);
    }

    private org.adridadou.ethereum.event.TransactionReceipt toReceipt(TransactionReceipt transactionReceipt) {
        Transaction tx = transactionReceipt.getTransaction();
        return new org.adridadou.ethereum.event.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()),EthAddress.of(tx.getReceiveAddress()), EthAddress.of(tx.getContractAddress()), transactionReceipt.getError(), EthData.of(transactionReceipt.getExecutionResult()), transactionReceipt.isSuccessful() && transactionReceipt.isValid());
    }
}
