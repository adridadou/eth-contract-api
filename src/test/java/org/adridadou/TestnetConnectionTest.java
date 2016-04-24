package org.adridadou;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.*;
import org.adridadou.ethereum.keystore.Keystore;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetConnectionTest {
    private static class TestNetConfig {
        private final String mordenConfig =
                "peer.discovery = {\n" +
                        "\n" +
                        "    # List of the peers to start\n" +
                        "    # the search of the online peers\n" +
                        "    # values: [ip:port, ip:port, ip:port ...]\n" +
                        "    ip.list = [\n" +
                        "        \"94.242.229.4:40404\",\n" +
                        "        \"94.242.229.203:30303\"\n" +
                        "    ]\n" +
                        "}\n" +
                        "\n" +
                        "# Network id\n" +
                        "peer.networkId = 2\n" +
                        "\n" +
                        "# Enable EIP-8\n" +
                        "peer.p2p.eip8 = true\n" +
                        "\n" +
                        "# the folder resources/genesis\n" +
                        "# contains several versions of\n" +
                        "# genesis configuration according\n" +
                        "# to the network the peer will run on\n" +
                        "genesis = frontier-morden.json\n" +
                        "\n" +
                        "# Blockchain settings (constants and algorithms) which are\n" +
                        "# not described in the genesis file (like MINIMUM_DIFFICULTY or Mining algorithm)\n" +
                        "blockchain.config.name = \"morden\"\n" +
                        "\n" +
                        "database {\n" +
                        "    # place to save physical storage files\n" +
                        "    dir = database-morden\n" +
                        "}\n";
        private final String testNetConfig =
                // network has no discovery, peers are connected directly
                "peer.discovery.enabled = false \n" +
                        // set port to 0 to disable accident inbound connections
                        "peer.listen.port = 0 \n" +
                        "peer.networkId = 161 \n" +
                        // a number of public peers for this network (not all of then may be functioning)
                        "peer.active = [" +
                        "    { url = 'enode://9bcff30ea776ebd28a9424d0ac7aa500d372f918445788f45a807d83186bd52c4c0afaf504d77e2077e5a99f1f264f75f8738646c1ac3673ccc652b65565c3bb@peer-1.ether.camp:30303' }," +
                        "    { url = 'enode://c2b35ed63f5d79c7f160d05c54dd60b3ba32d455dbb10a5fe6fde44854073db02f9a538423a63a480126c74c7f650d77066ae446258e3d00388401d419b99f88@peer-2.ether.camp:30303' }," +
                        "    { url = 'enode://8246787f8d57662b850b354f0b526251eafee1f077fc709460dc8788fa640a597e49ffc727580f3ebbbc5eacb34436a66ea40415fab9d73563481666090a6cf0@peer-3.ether.camp:30303' }" +
                        "] \n" +
                        "sync.enabled = true \n" +
                        // special genesis for this test network
                        "genesis = frontier-test.json \n" +
                        "database.dir = testnetSampleDb \n" +
                        "cache.flush.memory = 0 \n" +
                        "peer.p2p.eip8 = false";

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(mordenConfig.replaceAll("'", "\"")));
            return props;
        }
    }

    private EthereumProvider getMordenProvider(ECKey sender, TestEthereumListener listener) {

        Ethereum ethereum = EthereumFactory.createEthereum(TestNetConfig.class);
        ethereum.addListener(listener);
        ethereum.init();

        BlockchainProxy proxy = new BlockchainProxyImpl(ethereum, sender);
        return new EthereumProvider(new EthereumContractInvocationHandler(proxy), proxy);
    }

    @Test
    public void run() throws Exception {
        String contract =
                "contract myContract2 {" +
                        "  int i1;" +
                        "  function myMethod(int value) {i1 = value;}" +
                        "  function getI1() constant returns (int) {return i1;}" +
                        "}";
        String homeDir = System.getProperty("user.home");
        TestEthereumListener listener = new TestEthereumListener();
        EthereumProvider provider = getMordenProvider(ECKey.fromPrivate(SHA3Helper.sha3("cow".getBytes())), listener);

        while (!listener.isSyncDone()) {
            synchronized (this) {
                System.out.println("waiting for the sync to finish!");
                wait(20000);
            }
        }

        EthAddress address = provider.publishContract(contract);
        MyContract2 myContract = provider.createContractProxy(contract, address, MyContract2.class);

        myContract.myMethod(45);
        assertEquals(45, myContract.getI1());
    }

    private interface MyContract2 {
        void myMethod(int value);

        int getI1();
    }
}
