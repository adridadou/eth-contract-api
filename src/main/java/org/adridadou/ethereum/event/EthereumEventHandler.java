package org.adridadou.ethereum.event;

import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumEventHandler {
    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private final OnBlockHandler onBlockHandler;
    private final OnTransactionHandler onTransactionHandler;
    private long currentBlockNumber;

    public EthereumEventHandler() {
        this.onBlockHandler = new OnBlockHandler();
        this.onTransactionHandler = new OnTransactionHandler();
    }

    public void onBlock(OnBlockParameters block) {
        onBlockHandler.newBlock(block);
        currentBlockNumber = block.blockNumber;
    }

    public void onPendingTransactionUpdate(OnTransactionParameters tx) {
        onTransactionHandler.on(tx);
    }

    public void onTransactionExecuted(OnTransactionParameters tx, List<OnTransactionParameters> internalTxes) {
      internalTxes.forEach(onTransactionHandler::on);
      onTransactionHandler.on(tx);
    }

    public void onReady() {
        ready.complete(null);
    }

    public CompletableFuture<Void> ready() {
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
