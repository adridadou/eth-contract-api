package org.adridadou.ethereum.rpc.provider;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.ethj.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.values.config.*;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class InfuraRopstenEthereumFacadeProvider {

    public static EthereumFacade create(final InfuraKey key) {
        return new EthereumFacadeRpcProvider().create("https://ropsten.infura.io/" + key.key, EthereumFacadeProvider.ROPSTEN_CHAIN_ID);
    }
}
