package org.adridadou.ethereum.values.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 26.11.16.
 * This code is released under Apache 2 license
 */
public class BlockchainConfig {
    private  ChainId networkId;
    private  Boolean eip8;
    private  Boolean fastSync;
    private  GenesisPath genesis;
    private  EthereumConfigName configName;
    private  DatabaseDirectory dbDir;
    private  List<NodeIp> ipList;
    private  Boolean syncEnabled;
    private  Integer listenPort;
    private  Boolean peerDiscovery;
    private List<String> peerActiveList;
    private String peerPrivateKey;
    private IncompatibleDatabaseBehavior behavior;

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
                Optional.ofNullable(fastSync).map(fSync -> "sync.fast.enabled = " + fSync + "\n").orElse("") +
                Optional.ofNullable(peerPrivateKey).map(privateKey -> "peer.privateKey = " + privateKey + "\n").orElse("") +
                Optional.ofNullable(behavior).map(incompatibleDatabaseBehavior -> "database.incompatibleDatabaseBehavior = " + incompatibleDatabaseBehavior.name() + "\n").orElse("");
    }

    public static BlockchainConfig builder() {
        return new BlockchainConfig();
    }

    public BlockchainConfig networkId(ChainId networkId) {
        this.networkId = networkId;
        return this;
    }

    public BlockchainConfig eip8(boolean eip8) {
        this.eip8 = eip8;
        return this;
    }

    public BlockchainConfig fastSync(boolean fastSync) {
        this.fastSync = fastSync;
        return this;
    }

    public BlockchainConfig genesis(GenesisPath genesis) {
        this.genesis = genesis;
        return this;
    }

    public BlockchainConfig configName(EthereumConfigName configName) {
        this.configName = configName;
        return this;
    }

    public BlockchainConfig dbDirectory(DatabaseDirectory dbDir) {
        this.dbDir = dbDir;
        return this;
    }

    public BlockchainConfig addIp(NodeIp ip) {
        this.ipList = Optional.ofNullable(ipList).orElseGet(ArrayList::new);
        this.ipList.add(ip);
        return this;
    }

    public BlockchainConfig syncEnabled(boolean b) {
        this.syncEnabled = b;
        return this;
    }

    public BlockchainConfig listenPort(Integer port) {
        this.listenPort = port;
        return this;
    }

    public BlockchainConfig peerDiscovery(Boolean discovery) {
        this.peerDiscovery = discovery;
        return this;
    }

    public BlockchainConfig peerActiveUrl(String url) {
        this.peerActiveList = Optional.ofNullable(peerActiveList).orElseGet(ArrayList::new);
        this.peerActiveList.add("\"" + url + "\"");
        return this;
    }

    public BlockchainConfig privateKey(String privateKey) {
        this.peerPrivateKey = privateKey;
        return this;
    }

    public BlockchainConfig incompatibleDatabaseBehavior(IncompatibleDatabaseBehavior behavior) {
        this.behavior = behavior;
        return this;
    }
}
