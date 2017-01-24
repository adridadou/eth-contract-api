package org.adridadou.ethereum.swarm;

import org.spongycastle.util.encoders.Hex;

/**
 * Created by davidroon on 21.12.16.
 * This code is released under Apache 2 license
 */
public class SwarmHash {
    private final byte[] hash;

    public SwarmHash(byte[] hash) {
        this.hash = hash;
    }

    public static SwarmHash of(String hash) {
        return new SwarmHash(Hex.decode(hash));
    }

    public static SwarmHash of(byte[] hash) {
        return new SwarmHash(hash);
    }

    public String toString() {
        return Hex.toHexString(hash);
    }
}
