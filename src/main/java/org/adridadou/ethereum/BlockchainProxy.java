package org.adridadou.ethereum;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import rx.Observable;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SmartContract map(final String src, String contractName, EthAddress address, ECKey sender);

    SmartContract mapFromAbi(final String abi, EthAddress address, ECKey sender);

    Observable<EthAddress> publish(String code, String contractName, ECKey sender);

    Observable<TransactionReceipt> sendTx(long value, byte[] data, ECKey sender);

    EthereumEventHandler eventHandler();
}


