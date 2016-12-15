package org.adridadou.ethereum.provider;


import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.keystore.StringSecureKey;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.ethereum.values.config.DatabaseDirectory;
import org.adridadou.ethereum.values.config.EthereumConfigName;
import org.adridadou.ethereum.values.config.GenesisPath;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetEthereumFacadeProvider {


    public EthereumFacade create() {
        return create(new OnBlockHandler(), new OnTransactionHandler());
    }

    public EthereumFacade create(OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler) {

        return new GenericEthereumFacadeProvider().create(onBlockHandler, onTransactionHandler, BlockchainConfig.builder()
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
                .configName(EthereumConfigName.name("testnet"))
                .build());
    }

    public SecureKey getKey(String id) {
        return new StringSecureKey(id);
    }
}
