package org.adridadou;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.adridadou.util.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumProviderSpec {
    private final BlockchainProxy bcProxy = new BlockchainProxyTest();
    private final EthereumFacade ethereumProvider = new EthereumFacade(new EthereumContractInvocationHandler(bcProxy), bcProxy);

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

        assertEquals(23, proxy.myMethod());
    }

    @Test
    public void checkCreateTx() throws IOException, ExecutionException, InterruptedException {
        String contract =
                "contract myContract2 {" +
                        "  int i1;" +
                        "  function myMethod(int value) {i1 = value;}" +
                        "  function getI1() constant returns (int) {return i1;}" +
                        "}";

        EthAddress address = ethereumProvider.publishContract(contract);

        MyContract2 proxy = ethereumProvider.createContractProxy(contract, address, MyContract2.class);
        proxy.myMethod(12);

        assertEquals(12, proxy.getI1());
    }

    private interface MyContract {
        int myMethod();
    }

    private interface MyContract2 {
        void myMethod(int value);

        int getI1();
    }
}
