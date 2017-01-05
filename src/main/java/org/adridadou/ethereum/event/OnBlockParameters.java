package org.adridadou.ethereum.event;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

import java.util.List;

public class OnBlockParameters {
    public final Block block;
    public final List<TransactionReceipt> receipts;

    public OnBlockParameters(Block block, List<TransactionReceipt> receipts) {
        this.block = block;
        this.receipts = receipts;
    }

    @Override
    public String toString() {
        return "OnBlockParameters{" +
                "block=" + block +
                ", receipts=" + receipts +
                '}';
    }
}
