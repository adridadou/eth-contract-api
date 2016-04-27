package org.adridadou;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.provider.MainEthereumFacadeProvider;
import org.adridadou.ethereum.provider.MordenEthereumFacadeProvider;
import org.adridadou.ethereum.provider.TestnetEthereumFacadeProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetConnectionTest {
    private final TestnetEthereumFacadeProvider testnet = new TestnetEthereumFacadeProvider();
    private final MordenEthereumFacadeProvider morden = new MordenEthereumFacadeProvider();
    private final MainEthereumFacadeProvider main = new MainEthereumFacadeProvider();

    @Test
    public void run() throws Exception {
        run(testnet, "cow", "");
    }


    private void run(EthereumFacadeProvider ethereumFacadeProvider, final String id, final String password) throws Exception {
        String contract =
                "contract myContract2 {" +
                        "  int i1;" +
                        "  function myMethod(int value) {i1 = value;}" +
                        "  function getI1() constant returns (int) {return i1;}" +
                        "}";

        EthereumFacade provider = ethereumFacadeProvider.create(ethereumFacadeProvider.getKey(id, password));

        while (!provider.isSyncDone()) {
            synchronized (this) {
                wait(200);
            }
        }

        EthAddress address = provider.publishContract(contract);
        System.out.println("contract address:" + Hex.toHexString(address.address));
        MyContract2 myContract = provider.createContractProxy(contract, address, MyContract2.class);
        System.out.println("*** calling contract myMethod");
        myContract.myMethod(45);
        assertEquals(45, myContract.getI1());
    }

    private interface MyContract2 {
        void myMethod(int value);

        int getI1();
    }
}
