package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.EthereumApiException;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SmartContract map(final SoliditySource src, String contractName, EthAddress address, EthAccount sender);

    SmartContract mapFromAbi(final ContractAbi abi, EthAddress address, EthAccount sender);

    CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs);

    CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address);

    CompletableFuture<EthAddress> sendTx(final EthValue ethValue, final EthData data, final EthAccount sender);

    EthereumEventHandler events();

    boolean addressExists(EthAddress address);

    EthValue getBalance(EthAddress address);

    BigInteger getNonce(EthAddress address);

    default void hasEnoughFund(EthAddress address, EthValue requiredFund) {
        if (getBalance(address).compareTo(requiredFund) < 0) {
            throw new EthereumApiException("not enough fund for " + address.withLeading0x());
        }
    }
}


