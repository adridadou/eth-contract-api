package org.adridadou;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.EthereumJTest;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.SoliditySource;
import org.junit.Test;
import rx.Observable;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class EventsTest {
    private final EthereumJTest ethereumj = new EthereumJTest();
    private final EthereumFacade ethereum = EthereumFacadeProvider.forTest(ethereumj);
    private final EthAccount mainAccount = ethereumj.defaultAccount();
    private SoliditySource contractSource = SoliditySource.from("pragma solidity ^0.4.7;" +
            "contract contractEvents {" +
            "event MyEvent(string value);" +
            "function createEvent(string value) {" +
            "    MyEvent(value);" +
            "}" +
            "}");

    public EventsTest() throws URISyntaxException {
    }

    private EthAddress publishAndMapContract(EthereumFacade ethereum) throws Exception {
        CompiledContract compiledContract = ethereum.compile(contractSource, "contractEvents");
        CompletableFuture<EthAddress> futureAddress = ethereum.publishContract(compiledContract, mainAccount);
        return futureAddress.get();
    }

    @Test
    public void main_example_how_the_lib_works() throws Exception {
        EthAddress address = publishAndMapContract(ethereum);
        CompiledContract compiledContract = ethereum.compile(contractSource,"contractEvents");
        ContractEvents myContract = ethereum.createContractProxy(compiledContract, address, mainAccount, ContractEvents.class);
        Observable<MyEvent> observeEvent = ethereum.observeEvents(compiledContract.getAbi(), address, "MyEvent", MyEvent.class);
        CompletableFuture<Void> future = myContract.createEvent("my event is here");
        assertEquals("my event is here", observeEvent.toBlocking().first().value );
        future.get();
        ethereum.shutdown();

    }

    private interface ContractEvents {
        CompletableFuture<Void> createEvent(String value);
    }

    public static class MyEvent {
        private final String value;

        public MyEvent(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "MyEvent{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}
