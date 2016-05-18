package org.adridadou;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.provider.MainEthereumFacadeProvider;
import org.adridadou.ethereum.provider.MordenEthereumFacadeProvider;
import org.adridadou.ethereum.provider.TestnetEthereumFacadeProvider;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
@Ignore
public class TestnetConnectionTest {
    private final TestnetEthereumFacadeProvider testnet = new TestnetEthereumFacadeProvider();
    private final MordenEthereumFacadeProvider morden = new MordenEthereumFacadeProvider();
    private final MainEthereumFacadeProvider main = new MainEthereumFacadeProvider();

    @Test
    public void run() throws Exception {
        run(testnet, "cow", "");
    }


    private void run(EthereumFacadeProvider ethereumFacadeProvider, final String id, final String password) throws Exception {

        EthereumFacade provider = ethereumFacadeProvider.create(ethereumFacadeProvider.getKey(id, password));


        String contract = IOUtils.toString(new FileReader(new File("src/test/resources/contract.sol")));
        EthAddress address = provider.publishContract(contract);
        System.out.println("contract address:" + Hex.toHexString(address.address));
        MyContract2 myContract = provider.createContractProxy(contract, address, MyContract2.class);
        System.out.println("*** calling contract myMethod");
        myContract.myMethod("hello");
        assertEquals("hello", myContract.getI1());
        assertTrue(myContract.getT());
        assertEquals(provider.getSenderAddress(), myContract.getOwner());
        Integer[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertArrayEquals(expected, myContract.getArray().toArray(new Integer[0]));

        assertEquals(new MyReturnType(true, "hello", 34), myContract.getM());
    }

    public static class MyReturnType {
        private Boolean val1;
        private String val2;
        private Integer val3;

        public MyReturnType(Boolean val1, String val2, Integer val3) {
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
        }

        public Boolean getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }

        public Integer getVal3() {
            return val3;
        }

        @Override
        public String toString() {
            return "MyReturnType{" +
                    "val1=" + val1 +
                    ", val2='" + val2 + '\'' +
                    ", val3=" + val3 +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyReturnType that = (MyReturnType) o;

            if (val1 != null ? !val1.equals(that.val1) : that.val1 != null) return false;
            if (val2 != null ? !val2.equals(that.val2) : that.val2 != null) return false;
            return val3 != null ? val3.equals(that.val3) : that.val3 == null;

        }

        @Override
        public int hashCode() {
            int result = val1 != null ? val1.hashCode() : 0;
            result = 31 * result + (val2 != null ? val2.hashCode() : 0);
            result = 31 * result + (val3 != null ? val3.hashCode() : 0);
            return result;
        }
    }

    private interface MyContract2 {
        void myMethod(String value);

        String getI1();

        boolean getT();

        MyReturnType getM();

        EthAddress getOwner();

        List<Integer> getArray();

    }
}
