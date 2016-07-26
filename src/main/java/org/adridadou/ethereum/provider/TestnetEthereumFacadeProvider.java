package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.*;
import org.adridadou.ethereum.keystore.SecureKey;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetEthereumFacadeProvider implements EthereumFacadeProvider {

    private static class TestNetConfig {
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
            props.overrideParams(ConfigFactory.parseString(testNetConfig.replaceAll("'", "\"")));
            return props;
        }
    }

    @Override
    public EthereumFacade create() {
        Ethereum ethereum = EthereumFactory.createEthereum(TestNetConfig.class);
        EthereumListenerImpl ethereumListener = new EthereumListenerImpl(ethereum);
        ethereum.init();

        BlockchainProxy proxy = new BlockchainProxyImpl(ethereum, ethereumListener);
        return new EthereumFacade(new EthereumContractInvocationHandler(proxy), proxy);
    }

    @Override
    public ECKey getKey(String id, final String password) {
        return ECKey.fromPrivate(doSha3(id.getBytes()));
    }

    @Override
    public List<SecureKey> listAvailableKeys() {
        return new ArrayList<>();
    }

    private static byte[] doSha3(byte[] message) {
        SHA3Digest digest = new SHA3Digest(256);
        byte[] hash = new byte[digest.getDigestSize()];

        if (message.length != 0) {
            digest.update(message, 0, message.length);
        }
        digest.doFinal(hash, 0);
        return hash;
    }
}
