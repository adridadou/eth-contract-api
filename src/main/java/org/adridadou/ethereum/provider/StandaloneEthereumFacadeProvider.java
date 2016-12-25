package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.blockchain.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.keystore.StringSecureKey;

/**
 * Created by davidroon on 28.05.16.
 * This code is released under Apache 2 license
 */
public class StandaloneEthereumFacadeProvider implements EthereumFacadeProvider {
    public EthereumFacade create() {
        return new EthereumFacade(new BlockchainProxyTest());
    }

    public SecureKey getLockedAccount(String id) {
        return new StringSecureKey("");
    }

}
