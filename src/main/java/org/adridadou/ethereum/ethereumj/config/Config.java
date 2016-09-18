package org.adridadou.ethereum.ethereumj.config;

import java.util.List;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public class Config {
    private final SolidityCompilerConfig solc;
    private final MinerOptions minerOptions;
    private final SynchronizationConfig sync;
    private final CacheConfig cache;
    private final List<TrustedNode> trustedNodes;
    private final List<BootNodeConfig> bootNodeConfig;
    private final DatabaseConfig databaseConfig;
    private final PeerConfig peerConfig;
    private final VmConfig vmConfig;
    private final EthereumJConfig ethereumJConfig;

    public Config(SolidityCompilerConfig solc, MinerOptions minerOptions, SynchronizationConfig sync, CacheConfig cache, List<TrustedNode> trustedNodes, List<BootNodeConfig> bootNodeConfig, DatabaseConfig databaseConfig, PeerConfig peerConfig, VmConfig vmConfig, EthereumJConfig ethereumJConfig) {
        this.solc = solc;
        this.minerOptions = minerOptions;
        this.sync = sync;
        this.cache = cache;
        this.trustedNodes = trustedNodes;
        this.bootNodeConfig = bootNodeConfig;
        this.databaseConfig = databaseConfig;
        this.peerConfig = peerConfig;
        this.vmConfig = vmConfig;
        this.ethereumJConfig = ethereumJConfig;
    }
}
