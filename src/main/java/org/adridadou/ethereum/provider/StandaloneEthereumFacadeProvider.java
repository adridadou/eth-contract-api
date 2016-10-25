package org.adridadou.ethereum.provider;

import java.util.ArrayList;
import java.util.List;

import org.adridadou.ethereum.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.keystore.StringSecureKey;

/**
 * Created by davidroon on 28.05.16.
 * This code is released under Apache 2 license
 */
public class StandaloneEthereumFacadeProvider implements EthereumFacadeProvider {

    @Override
    public EthereumFacade create() {
        return create(new OnBlockHandler(), new OnTransactionHandler());
    }

    @Override
    public EthereumFacade create(OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler) {
        return new EthereumFacade(new BlockchainProxyTest());
    }

    @Override
    public SecureKey getKey(String id) throws Exception {
        return new StringSecureKey("");
    }

    @Override
    public List<FileSecureKey> listAvailableKeys() {
        return new ArrayList<>();
    }
}
