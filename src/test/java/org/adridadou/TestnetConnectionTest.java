package org.adridadou;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.BlockchainProxyImpl;
import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.ethereum.EthereumProvider;
import org.ethereum.config.net.MordenNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.junit.Test;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetConnectionTest {

    private EthereumProvider getMordenProvider(ECKey sender) {
        Ethereum ethereum = EthereumFactory.createEthereum(MordenNetConfig.class);
        ethereum.init();
        BlockchainProxy proxy = new BlockchainProxyImpl(ethereum, sender);
        return new EthereumProvider(new EthereumContractInvocationHandler(proxy), proxy);
    }

    @Test
    public void run() {

        //getMordenProvider();
    }
}
