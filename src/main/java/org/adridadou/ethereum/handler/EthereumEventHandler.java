package org.adridadou.ethereum.handler;

import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.spongycastle.util.encoders.Hex;
import rx.Observable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumEventHandler extends EthereumListenerAdapter {
    private final CompletableFuture<Boolean> futureSyncDone = new CompletableFuture<>();
    private final rx.Observable<Boolean> sync = Observable.from(futureSyncDone);
    private final OnBlockHandler onBlockHandler;
    private long currentBlockNumber;

    public EthereumEventHandler(Ethereum ethereum, OnBlockHandler onBlockHandler) {
        ethereum.addListener(this);
        this.onBlockHandler = onBlockHandler;
        currentBlockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        onBlockHandler.newBlock(new OnBlockParameters(block, receipts));
        currentBlockNumber = block.getNumber();
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
        futureSyncDone.complete(true);
    }

    public Observable<Boolean> observeSync() {
        return sync;
    }

    public long getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public Observable<OnBlockParameters> observeBlocks() {
        return onBlockHandler.observable;
    }
}

