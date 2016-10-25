package org.adridadou.ethereum;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.jsonrpc.JsonRpc;
import rx.Observable;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SmartContract map(final SoliditySource src, String contractName, EthAddress address, ECKey sender);

    SmartContract mapFromAbi(final ContractAbi abi, EthAddress address, ECKey sender);

    Observable<EthAddress> publish(SoliditySource code, String contractName, ECKey sender, Object... constructorArgs);

    Observable<TransactionReceipt> sendTx(long value, byte[] data, ECKey sender, EthAddress address);

    EthereumEventHandler events();

    boolean addressExists(EthAddress address);

    JsonRpc getJsonRpc();
}


