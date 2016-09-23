package org.adridadou.ethereum.ethereumj.config;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;

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
    private final PeerDiscoveryConfig peerDiscoveryConfig;
    private final VmConfig vmConfig;
    private final EthereumJConfig ethereumJConfig;

    public Config(SolidityCompilerConfig solc, MinerOptions minerOptions, SynchronizationConfig sync, CacheConfig cache, List<TrustedNode> trustedNodes, List<BootNodeConfig> bootNodeConfig, DatabaseConfig databaseConfig, PeerConfig peerConfig, PeerDiscoveryConfig peerDiscoveryConfig, VmConfig vmConfig, EthereumJConfig ethereumJConfig) {
        this.solc = solc;
        this.minerOptions = minerOptions;
        this.sync = sync;
        this.cache = cache;
        this.trustedNodes = trustedNodes;
        this.bootNodeConfig = bootNodeConfig;
        this.databaseConfig = databaseConfig;
        this.peerConfig = peerConfig;
        this.peerDiscoveryConfig = peerDiscoveryConfig;
        this.vmConfig = vmConfig;
        this.ethereumJConfig = ethereumJConfig;
    }

    @Override
    public String toString() {
        return "solc=" + solc +
                ", minerOptions=" + minerOptions +
                ", sync=" + sync +
                ", cache=" + cache +
                ", trustedNodes=" + trustedNodes +
                ", bootNodeConfig=" + bootNodeConfig +
                ", databaseConfig=" + databaseConfig +
                ", peerConfig=" + peerConfig +
                ", vmConfig=" + vmConfig +
                ", ethereumJConfig=" + ethereumJConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SolidityCompilerConfig solc;
        private MinerOptions minerOptions;
        private SynchronizationConfig sync;
        private CacheConfig cache;
        private List<TrustedNode> trustedNodes;
        private List<BootNodeConfig> bootNodeConfig;
        private DatabaseConfig databaseConfig;
        private PeerConfig peerConfig;
        private PeerDiscoveryConfig peerDiscoveryConfig;
        private VmConfig vmConfig;
        private EthereumJConfig ethereumJConfig;

        public Builder solc(SolidityCompilerConfig solc) {
            this.solc = solc;
            return this;
        }

        public Builder minerOptions(MinerOptions minerOptions) {
            this.minerOptions = minerOptions;
            return this;
        }

        public Builder sync(SynchronizationConfig sync) {
            this.sync = sync;
            return this;
        }

        public Builder cache(CacheConfig cache) {
            this.cache = cache;
            return this;
        }

        public Builder trustedNodes(List<TrustedNode> trustedNodes) {
            this.trustedNodes = trustedNodes;
            return this;
        }

        public Builder bootNodeConfig(List<BootNodeConfig> bootNodeConfig) {
            this.bootNodeConfig = bootNodeConfig;
            return this;
        }

        public Builder databaseConfig(DatabaseConfig databaseConfig) {
            this.databaseConfig = databaseConfig;
            return this;
        }

        public Builder peerConfig(PeerConfig peerConfig) {
            this.peerConfig = peerConfig;
            return this;
        }

        public Builder peerDiscoveryConfig(PeerDiscoveryConfig peerDiscoveryConfig) {
            this.peerDiscoveryConfig = peerDiscoveryConfig;
            return this;
        }

        public Builder vmConfig(VmConfig vmConfig) {
            this.vmConfig = vmConfig;
            return this;
        }

        public Builder ethereumJConfig(EthereumJConfig ethereumJConfig) {
            this.ethereumJConfig = ethereumJConfig;
            return this;
        }

        public Config build() {
            return new Config(solc, minerOptions, sync, cache, trustedNodes, bootNodeConfig, databaseConfig, peerConfig, peerDiscoveryConfig, vmConfig, ethereumJConfig);
        }

        public SystemProperties asSystemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(build().toString()));
            return props;
        }
    }
}
