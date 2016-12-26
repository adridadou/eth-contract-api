package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.values.config.*;

/**
 * Created by davidroon on 26.12.16.
 * This code is released under Apache 2 license
 */
public class EthereumJConfigs {

    public static BlockchainConfig.Builder mainNet() {
        return BlockchainConfig.builder();
    }

    public static BlockchainConfig.Builder ropsten() {
        return BlockchainConfig.builder()
                .addIp(NodeIp.ip("94.242.229.4:40404"))
                .addIp(NodeIp.ip("94.242.229.203:30303"))
                .networkId(EthereumFacadeProvider.ROPSTEN_CHAIN_ID)
                .eip8(true)
                .genesis(GenesisPath.path("ropsten.json"))
                .configName(EthereumConfigName.name("ropsten"))
                .dbDirectory(DatabaseDirectory.db("database-ropsten"));
    }

    public static BlockchainConfig.Builder etherCampTestnet() {
        return BlockchainConfig.builder()
                .eip8(false)
                .dbDirectory(DatabaseDirectory.db("ethercamp-test"))
                .genesis(GenesisPath.path("frontier-test.json"))
                .syncEnabled(true)
                .networkId(ChainId.id(161))
                .listenPort(0)
                .peerDiscovery(false)
                .peerActiveUrl("enode://9bcff30ea776ebd28a9424d0ac7aa500d372f918445788f45a807d83186bd52c4c0afaf504d77e2077e5a99f1f264f75f8738646c1ac3673ccc652b65565c3bb@peer-1.ether.camp:30303")
                .peerActiveUrl("enode://c2b35ed63f5d79c7f160d05c54dd60b3ba32d455dbb10a5fe6fde44854073db02f9a538423a63a480126c74c7f650d77066ae446258e3d00388401d419b99f88@peer-2.ether.camp:30303")
                .peerActiveUrl("enode://8246787f8d57662b850b354f0b526251eafee1f077fc709460dc8788fa640a597e49ffc727580f3ebbbc5eacb34436a66ea40415fab9d73563481666090a6cf0@peer-3.ether.camp:30303")
                .configName(EthereumConfigName.name("testnet"));
    }

    public static BlockchainConfig.Builder privateMiner(final DatabaseDirectory db) {
        return BlockchainConfig
                .builder()
                .peerDiscovery(false)
                .listenPort(30335)
                .privateKey("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec")
                .networkId(ChainId.id(55))
                .syncEnabled(false)
                .genesis(GenesisPath.path("private-genesis.json"))
                .dbDirectory(db);
        //.incompatibleDatabaseBehavior(IGNORE);
        /*
        "peer.discovery.enabled = false \n" +
                "peer.listen.port = 30335 \n" +
                // need to have different nodeId's for the peers
                "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                // our private net ID
                "peer.networkId = 555 \n" +
                // we have no peers to sync with
                "sync.enabled = false \n" +
                // genesis with a lower initial difficulty and some predefined known funded accounts
                "genesis = private-genesis.json \n" +
                // two peers need to have separate database dirs
                "database.dir = " + dbName + " \n" +
                // when more than 1 miner exist on the network extraData helps to identify the block creator
                "mine.extraDataHex = cccccccccccccccccccc \n" +
                "mine.cpuMineThreads = 2 \n" +
                "database.incompatibleDatabaseBehavior = IGNORE\n" +
                "cache.flush.blocks = 1";
                */
    }
}
