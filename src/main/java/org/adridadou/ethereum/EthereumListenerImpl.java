package org.adridadou.ethereum;

import com.google.common.collect.Lists;
import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumListenerImpl extends EthereumListenerAdapter {
    private Map<ByteArrayWrapper, CompletableFuture<TransactionReceipt>> txWaiters =
            Collections.synchronizedMap(new HashMap<>());
    CompletableFuture<Boolean> futureSyncDone = new CompletableFuture<>();

    public EthereumListenerImpl(Ethereum ethereum) {
        ethereum.addListener(this);
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        resolveOnHoldTransactions(receipts);
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        resolveOnHoldTransactions(Lists.newArrayList(txReceipt));
    }

    private void resolveOnHoldTransactions(List<TransactionReceipt> receipts) {
        for (TransactionReceipt receipt : receipts) {
            ByteArrayWrapper txHashW = new ByteArrayWrapper(receipt.getTransaction().getHash());
            if (txWaiters.containsKey(txHashW)) {
                txWaiters.get(txHashW).complete(receipt);
                txWaiters.remove(txHashW);
            }
        }
    }

    @Override
    public void onSyncDone() {
        futureSyncDone.complete(true);
    }

    boolean isSynced() {
        return futureSyncDone.isDone();
    }

    public CompletableFuture<TransactionReceipt> registerTx(Transaction txHash) {
        CompletableFuture<TransactionReceipt> futureTx = new CompletableFuture<>();
        txWaiters.put(new ByteArrayWrapper(txHash.getHash()), futureTx);
        return futureTx;
    }
}

