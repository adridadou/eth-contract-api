package org.adridadou.ethereum.keystore;

import org.adridadou.ethereum.values.EthAccount;

import java.io.File;

/**
 * Created by davidroon on 26.07.16.
 * This code is released under Apache 2 license
 */
public class FileSecureKey implements SecureKey {
    private final File keyfile;

    public FileSecureKey(File keyfile) {
        this.keyfile = keyfile;
    }

    public EthAccount decode(final String password) throws Exception {
        return new EthAccount(Keystore.fromKeystore(keyfile, password));
    }

    public String getName() {
        return keyfile.getName();
    }
}
