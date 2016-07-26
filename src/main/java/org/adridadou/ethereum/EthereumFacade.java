package org.adridadou.ethereum;

import org.ethereum.crypto.ECKey;

import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacade {
    private final EthereumContractInvocationHandler handler;
    private final BlockchainProxy blockchainProxy;

    public EthereumFacade(EthereumContractInvocationHandler handler, BlockchainProxy blockchainProxy) {
        this.handler = handler;
        this.blockchainProxy = blockchainProxy;
    }

    public <T> T createContractProxy(String code, String contractName, EthAddress address, ECKey sender, Class<T> contractInterface) throws IOException {
        waitForSyncDone();
        handler.register(contractInterface, code, contractName, address, sender);
        return (T) Proxy.newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
    }

    public EthAddress publishContract(String code, String contractName, ECKey sender) {
        waitForSyncDone();
        return blockchainProxy.publish(code, contractName, sender);
    }

    public boolean isSyncDone() {
        return blockchainProxy.isSyncDone();
    }

    public void waitForSyncDone() {
        while (!isSyncDone()) {
            synchronized (this) {
                try {
                    wait(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public long getCurrentBlockNumber() {
        return blockchainProxy.getCurrentBlockNumber();
    }
}
