package org.adridadou;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.provider.*;
import org.ethereum.crypto.ECKey;
import org.junit.Test;
import rx.Observable;
import rx.observables.BlockingObservable;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class TestnetConnectionTest {
    private final StandaloneEthereumFacadeProvider standalone = new StandaloneEthereumFacadeProvider();
    private final TestnetEthereumFacadeProvider testnet = new TestnetEthereumFacadeProvider();
    private final MordenEthereumFacadeProvider morden = new MordenEthereumFacadeProvider();
    private final MainEthereumFacadeProvider main = new MainEthereumFacadeProvider();

    @Test
    public void run() throws Exception {
        run(testnet, "cow", "");
    }


    private void run(EthereumFacadeProvider ethereumFacadeProvider, final String id, final String password) throws Exception {
        ECKey sender = ethereumFacadeProvider.getKey(id).decode(password);
        EthereumFacade ethereum = ethereumFacadeProvider.create();


        SoliditySource contract = SoliditySource.from(new File("src/test/resources/contract.sol"));
        Observable<EthAddress> address = ethereum.publishContract(contract, "myContract2", sender);
        MyContract2 myContract = ethereum.createContractProxy(contract, "myContract2", BlockingObservable.from(address).first(), sender, MyContract2.class);
        assertEquals("", myContract.getI1());
        //assertEquals(EthAddress.of(ethereumFacadeProvider.getKey(id).decode(password).getAddress()), myContract.getOwner());
        System.out.println("*** calling contract myMethod");
        Observable<Integer> observable = myContract.myMethod("this is a test");

        Integer result = BlockingObservable.from(observable).first();
        assertEquals(12, result.intValue());
        assertEquals("this is a test", myContract.getI1());
        assertTrue(myContract.getT());

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
        Observable<Integer> myMethod(String value);

        String getI1();

        boolean getT();

        MyReturnType getM();

        EthAddress getOwner();

        List<Integer> getArray();

    }
}
