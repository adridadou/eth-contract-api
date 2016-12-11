package org.adridadou.ethereum.values.config;

/**
 * Created by davidroon on 06.12.16.
 * This code is released under Apache 2 license
 */
public class ChainId {
    public final byte id;

    public ChainId(byte id) {
        this.id = id;
    }

    public static ChainId id(final byte id) {
        return new ChainId(id);
    }

    public static ChainId id(final int id) {
        return new ChainId((byte) id);
    }
}
