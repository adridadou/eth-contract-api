package org.adridadou.ethereum;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SmartContract map(final SoliditySource src, String contractName, EthAddress address, EthAccount sender);

    SmartContract mapFromAbi(final ContractAbi abi, EthAddress address, EthAccount sender);

    CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs);

    CompletableFuture<TransactionReceipt> sendTx(long value, byte[] data, EthAccount sender, EthAddress address);

    EthereumEventHandler events();

    boolean addressExists(EthAddress address);
}


