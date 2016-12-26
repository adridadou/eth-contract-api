package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.values.config.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 26.11.16.
 * This code is released under Apache 2 license
 */
public class BlockchainConfig {
    private final ChainId networkId;
    private final Boolean eip8;
    private final Boolean fastSync;
    private final GenesisPath genesis;
    private final EthereumConfigName configName;
    private final DatabaseDirectory dbDir;
    private final List<NodeIp> ipList;
    private final Boolean syncEnabled;
    private final Integer listenPort;
    private final Boolean peerDiscovery;
    private final List<String> peerActiveList;

    public BlockchainConfig(ChainId networkId, Boolean eip8, Boolean fastSync, GenesisPath genesis, EthereumConfigName configName, DatabaseDirectory dbDir, List<NodeIp> ipList, Boolean syncEnabled, Integer listenPort, Boolean peerDiscovery, List<String> peerActiveList) {
        this.networkId = networkId;
        this.eip8 = eip8;
        this.fastSync = fastSync;
        this.genesis = genesis;
        this.configName = configName;
        this.dbDir = dbDir;
        this.ipList = ipList;
        this.syncEnabled = syncEnabled;
        this.listenPort = listenPort;
        this.peerDiscovery = peerDiscovery;
        this.peerActiveList = peerActiveList;
    }

    public String toString() {
        final Optional<String> strIpList = Optional.ofNullable(ipList).map(lst -> String.join(",", lst.stream().map(NodeIp::toString).collect(Collectors.toList())));
        final Optional<String> strActivePeer = Optional.ofNullable(peerActiveList)
                .map(lst -> "[" + String.join(",", lst.stream()
                        .map(str -> "{ url = " + str + "}").collect(Collectors.toList())) + "]");
        return "peer.discovery = {\n" +
                strIpList.map(lst -> "ip.list = [\n" + lst + "]\n").orElse("") +
                Optional.ofNullable(peerDiscovery).map(discovery -> "enabled = " + discovery + "\n").orElse("") +
                "}\n" +
                strActivePeer.map(lst -> "peer.active = " + lst + "\n").orElse("") +
                Optional.ofNullable(listenPort).map(port -> "peer.listen.port = " + listenPort + "\n").orElse("") +
                Optional.ofNullable(networkId).map(id -> "peer.networkId = " + networkId.id + "\n").orElse("") +
                Optional.ofNullable(eip8).map(v -> "peer.p2p.eip8 = " + v + "\n").orElse("") +
                Optional.ofNullable(genesis).map(json -> "genesis = " + json.path + "\n").orElse("") +
                Optional.ofNullable(configName).map(config -> "blockchain.config.name = \"" + config.name + "\"\n").orElse("") +
                Optional.ofNullable(syncEnabled).map(sync -> "sync.enabled = " + sync + "\n").orElse("") +
                Optional.ofNullable(dbDir).map(db -> "database.dir = " + dbDir.directory + "\n").orElse("") +
                Optional.ofNullable(fastSync).map(db -> "sync.fast.enabled = " + fastSync + "\n").orElse("");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChainId networkId;
        private Boolean eip8;
        private Boolean fastSync;
        private GenesisPath genesis;
        private EthereumConfigName configName;
        private DatabaseDirectory dbDir;
        private List<NodeIp> ipList;
        private Boolean syncEnabled;
        private Integer listenPort;
        private Boolean peerDiscovery;
        private List<String> peerActiveList;

        public Builder networkId(ChainId networkId) {
            this.networkId = networkId;
            return this;
        }

        public Builder eip8(boolean eip8) {
            this.eip8 = eip8;
            return this;
        }

        public Builder fastSync(boolean fastSync) {
            this.fastSync = fastSync;
            return this;
        }

        public Builder genesis(GenesisPath genesis) {
            this.genesis = genesis;
            return this;
        }

        public Builder configName(EthereumConfigName configName) {
            this.configName = configName;
            return this;
        }

        public Builder dbDirectory(DatabaseDirectory dbDir) {
            this.dbDir = dbDir;
            return this;
        }

        public Builder addIp(NodeIp ip) {
            this.ipList = Optional.ofNullable(ipList).orElseGet(ArrayList::new);
            this.ipList.add(ip);
            return this;
        }

        public Builder syncEnabled(boolean b) {
            this.syncEnabled = b;
            return this;
        }

        public BlockchainConfig build() {
            return new BlockchainConfig(networkId, eip8, fastSync, genesis, configName, dbDir, ipList, syncEnabled, listenPort, peerDiscovery, peerActiveList);
        }

        public Builder listenPort(Integer port) {
            this.listenPort = port;
            return this;
        }

        public Builder peerDiscovery(Boolean discovery) {
            this.peerDiscovery = discovery;
            return this;
        }

        public Builder peerActiveUrl(String url) {
            this.peerActiveList = Optional.ofNullable(peerActiveList).orElseGet(ArrayList::new);
            this.peerActiveList.add("\"" + url + "\"");
            return this;
        }
    }
}
