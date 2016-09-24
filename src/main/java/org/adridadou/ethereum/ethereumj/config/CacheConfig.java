package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 *
 * # cache for blockchain run
 * # the flush hapens depending
 * # on memory usage or blocks
 * # threshhold. if both are specified
 * # memory will take precedence
 * cache {
 *
 * flush {
 *
 * # [0.7 = 70% memory to flush]
 * memory = 0
 *
 * # [10000 flush each 10000 blocks]
 * blocks = 1000
 * }
 * }
 */
public class CacheConfig {
    private final double memory;
    private final int blocks;

    public CacheConfig(double memory, int blocks) {
        this.memory = memory;
        this.blocks = blocks;
    }

    public double getMemory() {
        return memory;
    }

    public int getBlocks() {
        return blocks;
    }
}
