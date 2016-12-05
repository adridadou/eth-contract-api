package org.adridadou.ethereum.blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by davidroon on 26.11.16.
 * This code is released under Apache 2 license
 */
public class BlockchainConfig {
    private final Byte networkId;
    private final Boolean eip8;
    private final String genesis;
    private final String configName;
    private final String dbDir;
    private final List<String> ipList;
    private final Boolean syncEnabled;
    private final Integer listenPort;
    private final Boolean peerDiscovery;
    private final List<String> peerActiveList;

    public BlockchainConfig(Byte networkId, Boolean eip8, String genesis, String configName, String dbDir, List<String> ipList, Boolean syncEnabled, Integer listenPort, Boolean peerDiscovery, List<String> peerActiveList) {
        this.networkId = networkId;
        this.eip8 = eip8;
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
        final Optional<String> strIpList = Optional.ofNullable(ipList).map(lst -> String.join(",", lst));
        final Optional<String> strActivePeer = Optional.ofNullable(peerActiveList).map(lst -> String.join(",", lst));
        return "peer.discovery = {\n" +
                strIpList.map(lst -> "ip.list = [\n" + lst + "]\n").orElse("") +
                Optional.ofNullable(peerDiscovery).map(discovery -> "enabled = " + discovery + "\n").orElse("") +
                "}\n" +
                strActivePeer.map(lst -> "peer.active = " + lst + "\n").orElse("") +
                Optional.ofNullable(listenPort).map(port -> "peer.listen.port = " + listenPort + "\n").orElse("") +
                Optional.ofNullable(networkId).map(id -> "peer.networkId = " + networkId + "\n").orElse("") +
                Optional.ofNullable(eip8).map(v -> "peer.p2p.eip8 = " + v + "\n").orElse("") +
                Optional.ofNullable(genesis).map(json -> "genesis = " + json + "\n").orElse("") +
                Optional.ofNullable(configName).map(config -> "blockchain.config.name = \"" + config + "\"\n").orElse("") +
                Optional.ofNullable(syncEnabled).map(sync -> "sync.enabled = " + sync + "\n").orElse("") +
                Optional.ofNullable(dbDir).map(db -> "database {\n" + "dir = " + dbDir + "\n" + "}\n").orElse("");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Byte networkId;
        private boolean eip8;
        private String genesis;
        private String configName;
        private String dbDir;
        private List<String> ipList;
        private Boolean syncEnabled;
        private Integer listenPort;
        private Boolean peerDiscovery;
        private List<String> peerActiveList;

        public Builder networkId(byte networkId) {
            this.networkId = networkId;
            return this;
        }

        public Builder networkId(int networkId) {
            this.networkId = (byte) networkId;
            return this;
        }

        public Builder eip8(boolean eip8) {
            this.eip8 = eip8;
            return this;
        }

        public Builder genesis(String genesis) {
            this.genesis = genesis;
            return this;
        }

        public Builder configName(String configName) {
            this.configName = configName;
            return this;
        }

        public Builder dbDirectory(String dbDir) {
            this.dbDir = dbDir;
            return this;
        }

        public Builder addIp(String ip) {
            this.ipList = Optional.ofNullable(ipList).orElseGet(ArrayList::new);
            this.ipList.add("\"" + ip + "\"");
            return this;
        }

        public Builder syncEnabled(boolean b) {
            this.syncEnabled = b;
            return this;
        }

        public BlockchainConfig build() {
            return new BlockchainConfig(networkId, eip8, genesis, configName, dbDir, ipList, syncEnabled, listenPort, peerDiscovery, peerActiveList);
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
