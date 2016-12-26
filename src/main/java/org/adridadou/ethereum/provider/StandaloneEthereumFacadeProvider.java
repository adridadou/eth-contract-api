package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.blockchain.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumFacade;

/**
 * Created by davidroon on 28.05.16.
 * This code is released under Apache 2 license
 */
public class StandaloneEthereumFacadeProvider {
    public EthereumFacade create() {
        return new EthereumFacade(new BlockchainProxyTest());
    }
}
