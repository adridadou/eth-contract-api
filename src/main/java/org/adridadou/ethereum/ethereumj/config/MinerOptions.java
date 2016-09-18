package org.adridadou.ethereum.ethereumj.config;

import org.adridadou.ethereum.EthAddress;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * <p>
 * # miner options
 * mine {
 * <p>
 * # start mining blocks
 * # when 'sync.enabled' is true the mining starts when the sync is complete
 * # else the mining will start immediately, taking the best block from database
 * # (or genesis if no blocks exist yet)
 * start = false
 * <p>
 * # mining beneficiary
 * coinbase = "0000000000000000000000000000000000000000"
 * <p>
 * # extra data included in the mined block
 * # one of two properties should be specified
 * extraData = "EthereumJ powered"
 * #extraDataHex = "0102abcd"
 * <p>
 * # transactions with the gas price lower than this will not be
 * # included in mined blocks
 * # decimal number in weis
 * minGasPrice = 15000000000  # 15 Gwei
 * <p>
 * # minimal timeout between mined blocks
 * minBlockTimeoutMsec = 0
 * <p>
 * # number of CPU threads the miner will mine on
 * # 0 disables CPU mining
 * cpuMineThreads = 4
 * <p>
 * # there two options for CPU mining 'light' and 'full'
 * # 'light' requires only 16M of RAM but is much slower
 * # 'full' requires 1G of RAM and possibly ~7min for the DataSet generation
 * #   but is much faster during mining
 * fullDataSet = true
 * }
 */
public class MinerOptions {
    private final boolean start;
    private final EthAddress coinbase;
    private final String extraData; //TODO: should be either ExtraData or ExtraDataHex
    private final long minGasPrice;
    private final long minBlockTimeout;
    private final int cpuMineThreads;
    private final boolean fullDataSet;

    public MinerOptions(boolean start, EthAddress coinbase, String extraData, long minGasPrice, long minBlockTimeout, int cpuMineThreads, boolean fullDataSet) {
        this.start = start;
        this.coinbase = coinbase;
        this.extraData = extraData;
        this.minGasPrice = minGasPrice;
        this.minBlockTimeout = minBlockTimeout;
        this.cpuMineThreads = cpuMineThreads;
        this.fullDataSet = fullDataSet;
    }

    public boolean isStart() {
        return start;
    }

    public EthAddress getCoinbase() {
        return coinbase;
    }

    public String getExtraData() {
        return extraData;
    }

    public long getMinGasPrice() {
        return minGasPrice;
    }

    public long getMinBlockTimeout() {
        return minBlockTimeout;
    }

    public int getCpuMineThreads() {
        return cpuMineThreads;
    }

    public boolean isFullDataSet() {
        return fullDataSet;
    }
}
