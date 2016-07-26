package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.SecureKey;
import org.ethereum.crypto.ECKey;

import java.util.List;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface EthereumFacadeProvider {
    EthereumFacade create();

    ECKey getKey(final String id, final String password) throws Exception;

    List<SecureKey> listAvailableKeys();
}
