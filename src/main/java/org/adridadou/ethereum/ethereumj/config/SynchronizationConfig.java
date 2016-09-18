package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * <p>
 * # eth sync process
 * sync {
 * <p>
 * # block chain synchronization
 * # can be: [true/false]
 * enabled = true
 * <p>
 * # maximum blocks hashes to ask for by
 * # sending GET_BLOCK_HASHES msg
 * # we specify number of hashes we want
 * # to get; recommended value [1..1000]
 * # Default: unlimited
 * max.hashes.ask = 10000
 * <p>
 * # maximum blocks to ask,
 * # when downloading the chain
 * # sequentially sending GET_BLOCKS msg
 * # we specify number of blocks we want
 * # to get; recomended value [1..120]
 * max.blocks.ask = 100
 * <p>
 * # minimal peers count
 * # used in sync process
 * # sync may use more peers
 * # than this value
 * # but will try to get
 * # at least this many from discovery
 * peer.count = 30
 * <p>
 * # Uncomment this param
 * # to use a strict Eth version.
 * # Useful for testing
 * # version = 62
 * <p>
 * # exit if we receive a block that causes state conflict
 * # this option is mainly for debugging purposes
 * exitOnBlockConflict = false
 * }
 */
public class SynchronizationConfig {
    private final boolean exitOnBlockConflict;
    private final int version;
    private final int peerCount;
    private final int maxBlocksAsk;
    private final int maxHashesAsk;
    private final boolean enabled;

    public SynchronizationConfig(boolean exitOnBlockConflict, int version, int peerCount, int maxBlocksAsk, int maxHashesAsk, boolean enabled) {
        this.exitOnBlockConflict = exitOnBlockConflict;
        this.version = version;
        this.peerCount = peerCount;
        this.maxBlocksAsk = maxBlocksAsk;
        this.maxHashesAsk = maxHashesAsk;
        this.enabled = enabled;
    }

    public boolean isExitOnBlockConflict() {
        return exitOnBlockConflict;
    }

    public int getVersion() {
        return version;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public int getMaxBlocksAsk() {
        return maxBlocksAsk;
    }

    public int getMaxHashesAsk() {
        return maxHashesAsk;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
