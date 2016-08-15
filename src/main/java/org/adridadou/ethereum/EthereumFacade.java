package org.adridadou.ethereum;

import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.ethereum.crypto.ECKey;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacade {
    private final EthereumContractInvocationHandler handler;
    private EthereumListenerImpl ethereumListener;
    private final BlockchainProxy blockchainProxy;
    private final EthereumFacadeProvider provider;

    public EthereumFacade(EthereumContractInvocationHandler handler, EthereumListenerImpl ethereumListener, BlockchainProxy blockchainProxy, EthereumFacadeProvider provider) {
        this.handler = handler;
        this.ethereumListener = ethereumListener;
        this.blockchainProxy = blockchainProxy;
        this.provider = provider;
    }

    public <T> T createContractProxy(String code, String contractName, EthAddress address, ECKey sender, Class<T> contractInterface) throws IOException {
        handler.register(contractInterface, code, contractName, address, sender);
        return (T) Proxy.newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
    }

    public CompletableFuture<EthAddress> publishContract(String code, String contractName, ECKey sender) {
        return blockchainProxy.publish(code, contractName, sender);
    }

    public boolean isSyncDone() {
        return ethereumListener.isSynced();
    }

    public long getCurrentBlockNumber() {
        return blockchainProxy.getCurrentBlockNumber();
    }

    public List<? extends SecureKey> listAvailableKeys() {
        return provider.listAvailableKeys();
    }

    public SecureKey getKey(final String id) throws Exception {
        return provider.getKey(id);
    }
}
