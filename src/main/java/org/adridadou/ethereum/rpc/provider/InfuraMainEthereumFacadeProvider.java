package org.adridadou.ethereum.rpc.provider;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.ethj.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.values.config.InfuraKey;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class InfuraMainEthereumFacadeProvider {

    public EthereumFacade create(final InfuraKey key) {
        return new EthereumFacadeRpcProvider().create("https://main.infura.io/" + key.key, EthereumFacadeProvider.MAIN_CHAIN_ID);
    }
}
