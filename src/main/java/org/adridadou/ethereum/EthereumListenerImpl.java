package org.adridadou.ethereum;

import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumListenerImpl extends EthereumListenerAdapter {
    private boolean synced;
    private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<>());

    private final Ethereum ethereum;

    public EthereumListenerImpl(Ethereum ethereum) {
        this.ethereum = ethereum;
        ethereum.addListener(this);
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        System.out.println("*** new block received!!");
        for (TransactionReceipt receipt : receipts) {
            ByteArrayWrapper txHashW = new ByteArrayWrapper(receipt.getTransaction().getHash());
            if (txWaiters.containsKey(txHashW)) {
                txWaiters.put(txHashW, receipt);
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    @Override
    public void onSyncDone() {
        this.synced = true;
    }

    public boolean isSynced() {
        return synced;
    }

    public TransactionReceipt waitForTx(byte[] txHash) throws InterruptedException {
        ByteArrayWrapper txHashW = new ByteArrayWrapper(txHash);
        txWaiters.put(txHashW, null);
        long startBlock = ethereum.getBlockchain().getBestBlock().getNumber();
        while (true) {
            TransactionReceipt receipt = txWaiters.get(txHashW);
            if (receipt != null) {
                return receipt;
            } else {
                long curBlock = ethereum.getBlockchain().getBestBlock().getNumber();
                if (curBlock > startBlock + 16) {
                    throw new RuntimeException("The transaction was not included during last 16 blocks: " + txHashW.toString().substring(0, 8));
                }
            }
            synchronized (this) {
                wait(200);
            }
        }


    }
}

