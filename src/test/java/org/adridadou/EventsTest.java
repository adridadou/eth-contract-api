package org.adridadou;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.TestConfig;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.SoliditySource;
import org.junit.Test;
import rx.Observable;

import java.util.concurrent.CompletableFuture;

import static org.adridadou.ethereum.values.EthValue.ether;
import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class EventsTest {
    private final EthAccount mainAccount = AccountProvider.from("hello");
    private final EthereumFacade ethereum = EthereumFacadeProvider.forTest(TestConfig.builder()
            .balance(mainAccount, ether(1000))
            .build());
    private SoliditySource contractSource = SoliditySource.from("pragma solidity ^0.4.7;" +
            "contract contractEvents {" +
            "event MyEvent(string value);" +
            "function createEvent(string value) {" +
            "    MyEvent(value);" +
            "}" +
            "}");

    private EthAddress publishAndMapContract(EthereumFacade ethereum) throws Exception {
        CompiledContract compiledContract = ethereum.compile(contractSource, "contractEvents").get();
        CompletableFuture<EthAddress> futureAddress = ethereum.publishContract(compiledContract, mainAccount);
        return futureAddress.get();
    }

    @Test
    public void createTests() throws Exception {
        EthAddress address = publishAndMapContract(ethereum);
        CompiledContract compiledContract = ethereum.compile(contractSource,"contractEvents").get();
        ContractEvents myContract = ethereum.createContractProxy(compiledContract, address, mainAccount, ContractEvents.class);
        Observable<MyEvent> observeEvent = ethereum.observeEvents(compiledContract.getAbi(), address, "MyEvent", MyEvent.class);
        myContract.createEvent("my event is here");
        assertEquals("my event is here", observeEvent.toBlocking().first().value);
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
