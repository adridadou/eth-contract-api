package org.adridadou.ethereum.provider;

import java.util.List;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.SecureKey;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface EthereumFacadeProvider {

    EthereumFacade create();
    EthereumFacade create(OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler);

    SecureKey getKey(final String id) throws Exception;

    List<? extends SecureKey> listAvailableKeys();
}
