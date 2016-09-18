package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public enum BlockchainDataSource {
    LEVELDB("leveldb"), REDIS("redis"), MAPDB("mapdb");

    public final String ID;

    BlockchainDataSource(String id) {
        this.ID = id;
    }
}
