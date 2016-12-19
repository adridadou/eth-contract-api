package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 18.12.16.
 * This code is released under Apache 2 license
 */
public class SwarmMetadaLink {
    private final EthAddress hash;

    public SwarmMetadaLink(EthAddress hash) {
        this.hash = hash;
    }

    public EthAddress getHash() {
        return hash;
    }

    public static SwarmMetadaLink of(EthAddress address) {
        return new SwarmMetadaLink(address);
    }

    @Override
    public String toString() {
        return "bzzr0:" + hash;
    }
}
