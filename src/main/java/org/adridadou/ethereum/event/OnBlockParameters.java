package org.adridadou.ethereum.event;

import java.util.List;

public class OnBlockParameters {
    public final long blockNumber;
    public final List<TransactionReceipt> receipts;

    public OnBlockParameters(long blockNumber, List<TransactionReceipt> receipts) {
        this.blockNumber = blockNumber;
        this.receipts = receipts;
    }



    @Override
    public String toString() {
        return "OnBlockParameters{" +
                "blockNumber=" + blockNumber +
                ", receipts=" + receipts +
                '}';
    }
}
