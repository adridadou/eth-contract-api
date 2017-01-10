package org.adridadou;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.provider.PrivateEthereumFacadeProvider;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.SoliditySource;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import static org.adridadou.ethereum.provider.PrivateNetworkConfig.config;
import static org.adridadou.ethereum.values.EthValue.ether;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class EventsTest {
    private final PrivateEthereumFacadeProvider privateNetwork = new PrivateEthereumFacadeProvider();
    private final EthAccount mainAccount = AccountProvider.from("cow");
    private SoliditySource contract = SoliditySource.from(new File(this.getClass().getResource("/contractEvents.sol").toURI()));

    public EventsTest() throws URISyntaxException {
    }

    private EthereumFacade fromPrivateNetwork() {
        return privateNetwork.create(config()
                .reset(true)
                .initialBalance(mainAccount, ether(10)));
    }

    private EthAddress publishAndMapContract(EthereumFacade ethereum) throws Exception {
        CompletableFuture<EthAddress> futureAddress = ethereum.publishContract(contract, "contractEvents", mainAccount);
        return futureAddress.get();
    }

    @Test
    public void main_example_how_the_lib_works() throws Exception {
        final EthereumFacade ethereum = fromPrivateNetwork();
        EthAddress address = publishAndMapContract(ethereum);
        ContractEvents myContract = ethereum.createContractProxy(contract, "contractEvents", address, mainAccount, ContractEvents.class);

        ethereum.observeEvents(address, "MyEvent", MyEvent.class).forEach(event -> System.out.println("******" + event.toString()));

        myContract.createEvent("my event is here").get();
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
