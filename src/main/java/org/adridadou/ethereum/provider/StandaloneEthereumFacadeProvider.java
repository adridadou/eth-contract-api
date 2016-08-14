package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.keystore.StringSecureKey;

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
        return new EthereumFacade(new EthereumContractInvocationHandler(proxy), null, proxy, this);
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
