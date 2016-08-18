package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.SecureKey;

import java.util.List;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface EthereumFacadeProvider {
    EthereumFacade create();

    SecureKey getKey(final String id) throws Exception;

    List<? extends SecureKey> listAvailableKeys();
}
