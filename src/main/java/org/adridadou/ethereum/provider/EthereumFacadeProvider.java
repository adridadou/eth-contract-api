package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.keystore.SecureKey;

/**
 * Created by davidroon on 11.12.16.
 * This code is released under Apache 2 license
 */
public interface EthereumFacadeProvider {
    SecureKey getLockedAccount(final String id);
}
