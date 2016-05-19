package org.adridadou.ethereum;

import org.adridadou.TestnetConnectionTest;
import org.adridadou.util.BlockchainProxyTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by davidroon on 19.05.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacadeSpec {
    private BlockchainProxy proxy = new BlockchainProxyTest();
    private EthereumContractInvocationHandler handler = new EthereumContractInvocationHandler(proxy);
    private EthereumFacade ethereum = new EthereumFacade(handler, proxy);

    @Test
    public void testReturnTypeConverters() throws Throwable {
        String contract = IOUtils.toString(new FileReader(new File("src/test/resources/contract2.sol")));
        EthAddress address = ethereum.publishContract(contract);
        MyContract2 myContract = ethereum.createContractProxy(contract, address, MyContract2.class);
        System.out.println("*** calling contract myMethod");
        assertEquals("hello", myContract.getI1());
        assertTrue(myContract.getT());
        Integer[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertArrayEquals(expected, myContract.getArray().toArray(new Integer[0]));

        assertEquals(new MyReturnType(true, "hello", 34), myContract.getM());

    }

    private interface MyContract2 {
        void myMethod(String value);

        String getI1();

        boolean getT();

        MyReturnType getM();

        List<Integer> getArray();
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
}
