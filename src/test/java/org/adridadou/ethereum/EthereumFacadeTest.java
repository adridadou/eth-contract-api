package org.adridadou.ethereum;

import org.adridadou.ethereum.blockchain.BlockchainProxy;
import org.adridadou.ethereum.blockchain.BlockchainProxyTest;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.SoliditySource;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by davidroon on 19.05.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacadeTest {
    private final BlockchainProxy proxy = new BlockchainProxyTest();
    private final EthereumFacade ethereum = new EthereumFacade(proxy, new InputTypeHandler(), new OutputTypeHandler(), SwarmService.from(SwarmService.PUBLIC_HOST));
    private final EthAccount sender = null;

    @Before
    public void before() {
        ethereum.addInputHandlers(InputTypeHandler.JAVA_INPUT_CONVERTERS)
                .addOutputHandlers(OutputTypeHandler.JAVA_OUTPUT_CONVERTERS);
    }

    @Test
    public void testReturnTypeConverters() throws Throwable {
        SoliditySource contractSource = SoliditySource.from(new File("src/test/resources/contract2.sol"));
        CompiledContract compiledContract = ethereum.compile(contractSource, "myContract2");
        EthAddress address = ethereum.publishContract(compiledContract, sender).get();
        MyContract2 myContract = ethereum.createContractProxy(compiledContract, address, sender, MyContract2.class);
        System.out.println("*** calling contract myMethod");
        assertEquals("hello", myContract.getI1());
        assertTrue(myContract.getT());
        Integer[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Long[] expected2 = {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        assertArrayEquals(expected2, myContract.getArray().toArray(new Long[0]));
        assertArrayEquals(expected, myContract.getArray2());

        assertEquals(new MyReturnType(true, "hello", 34), myContract.getM());

    }

    private interface MyContract2 {
        String getI1();

        boolean getT();

        MyReturnType getM();

        List<Long> getArray();

        Integer[] getArray2();

        EthAddress getOwner();
    }

    public static class MyReturnType {
        private final Boolean val1;
        private final String val2;
        private final Integer val3;

        public MyReturnType(final Boolean val1, final String val2, final Integer val3) {
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
