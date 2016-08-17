package org.adridadou.ethereum;

import org.adridadou.ethereum.smartcontract.SolidityContract;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SolidityContract map(final String src, String contractName, EthAddress address, ECKey sender);

    SolidityContract mapFromAbi(final String abi, EthAddress address, ECKey sender);

    CompletableFuture<EthAddress> publish(String code, String contractName, ECKey sender);

    long getCurrentBlockNumber();

    CompletableFuture<TransactionReceipt> sendTx(long value, byte[] data, ECKey sender) throws InterruptedException;
}


