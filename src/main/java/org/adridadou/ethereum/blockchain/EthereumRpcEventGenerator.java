package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.ethereum.listener.EthereumListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidroon on 30.01.17.
 */
public class EthereumRpcEventGenerator {
    private List<EthereumListener> ethereumEventHandlers = new ArrayList<>();

    public void addListener(EthereumEventHandler ethereumEventHandler) {

        this.ethereumEventHandlers.add(ethereumEventHandler);
    }
}
