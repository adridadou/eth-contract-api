package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthereumFacade;
import org.ethereum.crypto.ECKey;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface EthereumFacadeProvider {
    EthereumFacade create(final ECKey key);

    ECKey getKey(final String id, final String password) throws Exception;
}
