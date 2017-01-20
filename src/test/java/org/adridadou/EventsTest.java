package org.adridadou;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.provider.PrivateEthereumFacadeProvider;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.SoliditySource;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import static org.adridadou.ethereum.provider.PrivateNetworkConfig.config;
import static org.adridadou.ethereum.values.EthValue.ether;
import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class EventsTest {
    private final PrivateEthereumFacadeProvider privateNetwork = new PrivateEthereumFacadeProvider();
    private final EthAccount mainAccount = AccountProvider.from("cow");
    private SoliditySource contractSource = SoliditySource.from("pragma solidity ^0.4.7;" +
            "contract contractEvents {" +
            "event MyEvent(string value);" +
            "function createEvent(string value) {" +
            "    MyEvent(value);" +
            "}" +
            "}");

    public EventsTest() throws URISyntaxException {
    }

    private EthereumFacade fromPrivateNetwork() {
        return privateNetwork.create(config()
                .reset(true)
                .initialBalance(mainAccount, ether(10)));
    }

    private EthAddress publishAndMapContract(EthereumFacade ethereum) throws Exception {
        CompiledContract compiledContract = ethereum.compile(contractSource, "contractEvents");
        CompletableFuture<EthAddress> futureAddress = ethereum.publishContract(compiledContract, mainAccount);
        return futureAddress.get();
    }

    @Test
    public void eventTests() throws Exception {
        final EthereumFacade ethereum = fromPrivateNetwork();
        EthAddress address = publishAndMapContract(ethereum);
        CompiledContract compiledContract = ethereum.compile(contractSource,"contractEvents");
        ContractEvents myContract = ethereum.createContractProxy(compiledContract, address, mainAccount, ContractEvents.class);

        CompletableFuture<Void> future = myContract.createEvent("my event is here");
        assertEquals("my event is here", ethereum.observeEvents(compiledContract.getAbi(), address, "MyEvent", MyEvent.class).toBlocking().first().value);
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
