package org.adridadou;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.provider.MainEthereumFacadeProvider;
import org.adridadou.ethereum.provider.MordenEthereumFacadeProvider;
import org.adridadou.ethereum.provider.TestnetEthereumFacadeProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
                        "  string i1;" +
                        "address owner;" +
                        "  function myMethod(string value) {" +
                        "i1 = value;" +
                        "owner = msg.sender;" +
                        "}" +
                        "  function getI1() constant returns (string) {return i1;}" +
                        "  function getT() constant returns (bool) {return true;}" +
                        "  function getOwner() constant returns (address) {return owner;}" +
                        "}";

        EthereumFacade provider = ethereumFacadeProvider.create(ethereumFacadeProvider.getKey(id, password));

        provider.waitForSyncDone();

        EthAddress address = provider.publishContract(contract);
        System.out.println("contract address:" + Hex.toHexString(address.address));
        MyContract2 myContract = provider.createContractProxy(contract, address, MyContract2.class);
        System.out.println("*** calling contract myMethod");
        myContract.myMethod("hello");
        assertEquals("hello", myContract.getI1());
        assertTrue(myContract.getT());
        assertEquals(provider.getSenderAddress(), myContract.getOwner());
    }

    private interface MyContract2 {
        void myMethod(String value);

        String getI1();

        boolean getT();

        EthAddress getOwner();
    }
}
