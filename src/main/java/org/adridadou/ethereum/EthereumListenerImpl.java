package org.adridadou.ethereum;

import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
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
    private boolean synced;
    private Map<ByteArrayWrapper, CompletableFuture<TransactionReceipt>> txWaiters =
            Collections.synchronizedMap(new HashMap<>());

    public EthereumListenerImpl(Ethereum ethereum) {
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
                txWaiters.get(txHashW).complete(receipt);
                txWaiters.remove(txHashW);
            }
        }
    }

    @Override
    public void onSyncDone() {
        this.synced = true;
    }

    boolean isSynced() {
        return synced;
    }

    public CompletableFuture<TransactionReceipt> registerTx(byte[] txHash) throws InterruptedException {
        CompletableFuture<TransactionReceipt> futureTx = new CompletableFuture<>();
        txWaiters.put(new ByteArrayWrapper(txHash), futureTx);
        return futureTx;

    }
}

