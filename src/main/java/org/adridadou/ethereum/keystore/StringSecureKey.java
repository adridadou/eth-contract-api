package org.adridadou.ethereum.keystore;

import org.ethereum.crypto.ECKey;
import org.spongycastle.crypto.digests.SHA3Digest;

/**
 * Created by davidroon on 28.07.16.
 * This code is released under Apache 2 license
 */
public class StringSecureKey implements SecureKey {

    private final String id;

    public StringSecureKey(String id) {
        this.id = id;
    }

    @Override
    public ECKey decode(String password) throws Exception {
        return ECKey.fromPrivate(doSha3(id.getBytes()));
    }

    @Override
    public String getName() {
        return null;
    }

    private static byte[] doSha3(byte[] message) {
        SHA3Digest digest = new SHA3Digest(256);
        byte[] hash = new byte[digest.getDigestSize()];

        if (message.length != 0) {
            digest.update(message, 0, message.length);
        }
        digest.doFinal(hash, 0);
        return hash;
    }
}
