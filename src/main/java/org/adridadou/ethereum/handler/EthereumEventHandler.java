package org.adridadou.ethereum.handler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.Block;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.facade.Ethereum;
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
    private OnTransactionHandler onTransactionHandler;
    private long currentBlockNumber;

    public EthereumEventHandler(Ethereum ethereum, OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler) {
        ethereum.addListener(this);
        this.onBlockHandler = onBlockHandler;
        this.onTransactionHandler = onTransactionHandler;
        currentBlockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        onBlockHandler.newBlock(new OnBlockParameters(block, receipts));
        currentBlockNumber = block.getNumber();
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        onTransactionHandler.onTransaction(new OnTransactionParameters(txReceipt.getTransaction(), state));
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        onTransactionHandler.onTransaction(new OnTransactionParameters(summary.getTransaction(), PendingTransactionState.INCLUDED));
    }

    public TransactionReceipt checkForErrors(final TransactionReceipt receipt) {
        if (receipt.isSuccessful() && receipt.isValid()) {
            return receipt;
        } else {
            throw new EthereumApiException("error with the transaction " + Hex.toHexString(receipt.getTransaction().getHash()) + ". error:" + receipt.getError());
        }
    }

    @Override
    public void onSyncDone() {
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
