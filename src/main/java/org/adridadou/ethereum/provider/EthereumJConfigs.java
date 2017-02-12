package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.ethj.BlockchainConfig;
import org.adridadou.ethereum.ethj.IncompatibleDatabaseBehavior;
import org.adridadou.ethereum.values.config.*;

import static org.adridadou.ethereum.provider.EthereumFacadeProvider.ETHER_CAMP_CHAIN_ID;
import static org.adridadou.ethereum.provider.EthereumFacadeProvider.ROPSTEN_CHAIN_ID;

/**
 * Created by davidroon on 26.12.16.
 * This code is released under Apache 2 license
 */
public class EthereumJConfigs {

    private static final ChainId PRIVATE_NETWORK_CHAIN_ID = ChainId.id(55);
    public static final int MINER_PORT = 30335;

    private EthereumJConfigs() {}

    public static BlockchainConfig mainNet() {
        return BlockchainConfig.builder();
    }

    public static BlockchainConfig ropsten() {
        return BlockchainConfig.builder()
                .addIp(NodeIp.ip("94.242.229.4:40404"))
                .addIp(NodeIp.ip("94.242.229.203:30303"))
                .networkId(ROPSTEN_CHAIN_ID)
                .eip8(true)
                .genesis(GenesisPath.path("ropsten.json"))
                .configName(EthereumConfigName.name("ropsten"))
                .dbDirectory(DatabaseDirectory.db("database-ropsten"));
    }

    public static BlockchainConfig etherCampTestnet() {
        return BlockchainConfig.builder()
                .eip8(false)
                .dbDirectory(DatabaseDirectory.db("ethercamp-test"))
                .genesis(GenesisPath.path("frontier-test.json"))
                .syncEnabled(true)
                .networkId(ETHER_CAMP_CHAIN_ID)
                .listenPort(0)
                .peerDiscovery(false)
                .peerActiveUrl("enode://9bcff30ea776ebd28a9424d0ac7aa500d372f918445788f45a807d83186bd52c4c0afaf504d77e2077e5a99f1f264f75f8738646c1ac3673ccc652b65565c3bb@peer-1.ether.camp:30303")
                .peerActiveUrl("enode://c2b35ed63f5d79c7f160d05c54dd60b3ba32d455dbb10a5fe6fde44854073db02f9a538423a63a480126c74c7f650d77066ae446258e3d00388401d419b99f88@peer-2.ether.camp:30303")
                .peerActiveUrl("enode://8246787f8d57662b850b354f0b526251eafee1f077fc709460dc8788fa640a597e49ffc727580f3ebbbc5eacb34436a66ea40415fab9d73563481666090a6cf0@peer-3.ether.camp:30303")
                .configName(EthereumConfigName.name("testnet"));
    }

    public static BlockchainConfig privateMiner() {
        return BlockchainConfig
                .builder()
                .peerDiscovery(false)
                .listenPort(MINER_PORT)
                .privateKey("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec")
                .networkId(PRIVATE_NETWORK_CHAIN_ID)
                .syncEnabled(false)
                .genesis(GenesisPath.path("private-genesis.json"))
                .incompatibleDatabaseBehavior(IncompatibleDatabaseBehavior.IGNORE);
    }
}
