package org.adridadou.ethereum.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.adridadou.ethereum.blockchain.Ethereumj;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;
import org.spongycastle.util.encoders.Hex;
import rx.Observable;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumEventHandler extends EthereumListenerAdapter {
    private final CompletableFuture<Boolean> ready = new CompletableFuture<>();
    private final OnBlockHandler onBlockHandler;
    private final OnTransactionHandler onTransactionHandler;
    private long currentBlockNumber;

    public EthereumEventHandler(Ethereumj ethereum) {
        ethereum.addListener(this);
        this.onBlockHandler = new OnBlockHandler();
        this.onTransactionHandler = new OnTransactionHandler();
        currentBlockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        onBlockHandler.newBlock(new OnBlockParameters(block, receipts));
        currentBlockNumber = block.getNumber();
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        TransactionStatus transactionStatus = null;

        switch(state) {
          case PENDING:
          case NEW_PENDING: transactionStatus = TransactionStatus.Pending; break;
          case DROPPED: transactionStatus = TransactionStatus.Dropped; break;
          case INCLUDED: transactionStatus = TransactionStatus.Included;break;
        }
        Transaction transaction = txReceipt.getTransaction();
        onTransactionHandler.on(new OnTransactionParameters(txReceipt, EthData.of(transaction.getHash()), transactionStatus, txReceipt.getError(), new ArrayList<>(), transaction.getSender(), transaction.getReceiveAddress()));
    }

  @Override
  public void onTransactionExecuted(TransactionExecutionSummary summary) {
      summary.getInternalTransactions()
              .forEach(internalTransaction -> onTransactionHandler
                      .on(new OnTransactionParameters(null, EthData.of(internalTransaction.getHash()), TransactionStatus.Executed, "", summary.getLogs(), internalTransaction.getSender(), internalTransaction.getReceiveAddress())));
      Transaction transaction = summary.getTransaction();
      onTransactionHandler.on(new OnTransactionParameters(null, EthData.of(transaction.getHash()), TransactionStatus.Executed, "", summary.getLogs(), transaction.getSender(), transaction.getReceiveAddress()));
  }

  public TransactionReceipt checkForErrors(final TransactionReceipt receipt) {
        if (receipt.isSuccessful() && receipt.isValid()) {
            return receipt;
        } else {
            throw new EthereumApiException("error with the transaction " + Hex.toHexString(receipt.getTransaction().getHash()) + ". error:" + receipt.getError());
        }
    }

    @Override
    public void onSyncDone(final SyncState syncState) {
        ready.complete(Boolean.TRUE);
    }

    public CompletableFuture<Boolean> onReady() {
        return ready;
    }

    public long getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public Observable<OnBlockParameters> observeBlocks() {
        return onBlockHandler.observable;
    }

    public Observable<OnTransactionParameters> observeTransactions() {
        return onTransactionHandler.observable;
    }

}
