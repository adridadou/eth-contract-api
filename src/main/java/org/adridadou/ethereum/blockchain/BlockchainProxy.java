package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.EthereumApiException;
import rx.Observable;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SmartContract mapFromAbi(final ContractAbi abi, EthAddress address, EthAccount sender);

    CompletableFuture<EthAddress> publish(CompiledContract contract, EthAccount sender, Object... constructorArgs);

    CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address);
    CompletableFuture<EthAddress> sendTx(final EthValue ethValue, final EthData data, final EthAccount sender);

    EthereumEventHandler events();

    boolean addressExists(EthAddress address);

    EthValue getBalance(EthAddress address);

    BigInteger getNonce(EthAddress address);

    SmartContractByteCode getCode(EthAddress address);

    default void hasEnoughFund(EthAddress address, EthValue requiredFund) {
        if (getBalance(address).compareTo(requiredFund) < 0) {
            throw new EthereumApiException("not enough fund for " + address.withLeading0x());
        }
    }

    <T> Observable<T> observeEvents(EthAddress contractAddress, String eventName, Class<T> cls);

    void shutdown();
}


