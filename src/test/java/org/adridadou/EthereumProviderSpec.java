package org.adridadou;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.adridadou.util.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumProvider;
import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.util.EthereumConfigTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumProviderSpec {
    private final BlockchainProxy bcProxy = new BlockchainProxyTest();
    private final EthereumConfigTest config = new EthereumConfigTest();
    private final EthereumProvider ethereumProvider = new EthereumProvider(config, new EthereumContractInvocationHandler(bcProxy), bcProxy);

    @Test
    public void checkSuccessCase() throws IOException {
        String contract =
                "contract myContract {" +
                        "  int i1;" +
                        "  function myMethod() returns (int) {" +
                        "    return 23;" +
                        "  }" +
                        "}";

        EthAddress address = ethereumProvider.publishContract(contract);

        MyContract proxy = ethereumProvider.createContractProxy(contract, address, MyContract.class);

        Assert.assertEquals(23, proxy.myMethod());
    }

    private interface MyContract {
        int myMethod();
    }
}
