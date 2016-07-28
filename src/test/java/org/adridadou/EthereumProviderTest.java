package org.adridadou;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.adridadou.ethereum.BlockchainProxyTest;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.ethereum.crypto.ECKey;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumProviderTest {
    private final BlockchainProxy bcProxy = new BlockchainProxyTest();
    private final ECKey sender = null;
    private final EthereumFacade ethereum = new EthereumFacade(new EthereumContractInvocationHandler(bcProxy), bcProxy, null);

    @Test
    public void checkSuccessCase() throws IOException {
        String contract =
                "contract myContract {" +
                        "  int i1;" +
                        "  function myMethod() returns (int) {" +
                        "    return 23;" +
                        "  }" +
                        "}";

        EthAddress address = ethereum.publishContract(contract, "myContract", sender);

        MyContract proxy = ethereum.createContractProxy(contract, "myContract", address, sender, MyContract.class);

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

        EthAddress address = ethereum.publishContract(contract, "myContract2", sender);

        MyContract2 proxy = ethereum.createContractProxy(contract, "myContract2", address, sender, MyContract2.class);
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
