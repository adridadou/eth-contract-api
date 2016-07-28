package org.adridadou.ethereum.keystore;

import org.ethereum.crypto.ECKey;

/**
 * Created by davidroon on 28.07.16.
 * This code is released under Apache 2 license
 */
public interface SecureKey {
    ECKey decode(final String password) throws Exception;

    String getName();
}
