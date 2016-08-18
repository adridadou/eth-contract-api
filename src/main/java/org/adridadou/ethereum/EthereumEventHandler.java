package org.adridadou.ethereum;

import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
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
    private Map<ByteArrayWrapper, CompletableFuture<TransactionReceipt>> txWaiters =
            Collections.synchronizedMap(new HashMap<>());
    private final CompletableFuture<Boolean> futureSyncDone = new CompletableFuture<>();
    private final rx.Observable<Boolean> sync = Observable.from(futureSyncDone);
    private long currentBlockNumber;

    public EthereumEventHandler(Ethereum ethereum) {
        ethereum.addListener(this);
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        resolveOnHoldTransactions(receipts);
    }

    private void resolveOnHoldTransactions(List<TransactionReceipt> receipts) {
        for (TransactionReceipt receipt : receipts) {
            ByteArrayWrapper txHashW = new ByteArrayWrapper(receipt.getTransaction().getHash());
            if (txWaiters.containsKey(txHashW)) {
                CompletableFuture<TransactionReceipt> current = txWaiters.get(txHashW);
                if (receipt.isSuccessful() && receipt.isValid()) {
                    current.complete(receipt);
                } else {
                    current.completeExceptionally(new EthereumApiException("error with the transaction " + Hex.toHexString(receipt.getTransaction().getHash()) + ". error:" + receipt.getError()));
                }
                txWaiters.remove(txHashW);
            }
        }
    }

    @Override
    public void onSyncDone() {
        futureSyncDone.complete(true);
    }


    public Observable<TransactionReceipt> registerTx(Transaction txHash) {
        CompletableFuture<TransactionReceipt> futureTx = new CompletableFuture<>();
        txWaiters.put(new ByteArrayWrapper(txHash.getHash()), futureTx);
        return Observable.from(futureTx);
    }


    public Observable<Boolean> getSync() {
        return sync;
    }

    public long getCurrentBlockNumber() {
        return currentBlockNumber;
    }
}

