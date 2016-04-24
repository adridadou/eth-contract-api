package org.adridadou;

import org.ethereum.core.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;

import java.util.List;

/**
 * Created by davidroon on 21.04.16.
 * This code is released under Apache 2 license
 */
public class TestEthereumListener implements EthereumListener {
    private boolean syncDone;

    @Override
    public void trace(String output) {

    }

    @Override
    public void onNodeDiscovered(Node node) {

    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {

    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage status) {

    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {

    }

    @Override
    public void onSendMessage(Channel channel, Message message) {

    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

    }

    @Override
    public void onPeerDisconnect(String host, long port) {

    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {

    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {

    }

    @Override
    public void onSyncDone() {
        System.out.println("************* sync is done! ************");
        syncDone = true;
    }

    @Override
    public void onNoConnections() {

    }

    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {

    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {

    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {

    }

    @Override
    public void onLongSyncDone() {
        System.out.println("************* long sync is done! ************");
        syncDone = true;
    }

    @Override
    public void onLongSyncStarted() {
        System.out.println("************* long sync is started! ************");
    }

    public boolean isSyncDone() {
        return syncDone;
    }
}
