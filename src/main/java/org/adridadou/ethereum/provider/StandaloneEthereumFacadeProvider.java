package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.SecureKey;
import org.ethereum.crypto.ECKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidroon on 28.05.16.
 * This code is released under Apache 2 license
 */
public class StandaloneEthereumFacadeProvider implements EthereumFacadeProvider {
    @Override
    public EthereumFacade create() {
        BlockchainProxyTest proxy = new BlockchainProxyTest();
        return new EthereumFacade(new EthereumContractInvocationHandler(proxy), proxy);
    }

    @Override
    public ECKey getKey(String id, String password) throws Exception {
        return new ECKey();
    }

    @Override
    public List<SecureKey> listAvailableKeys() {
        return new ArrayList<>();
    }
}
