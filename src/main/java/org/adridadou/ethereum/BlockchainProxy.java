package org.adridadou.ethereum;

import org.ethereum.util.blockchain.SolidityContract;

import java.util.concurrent.Future;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    void call(SolidityContract contract, String functionName, Object[] args);

    Object[] callConst(SolidityContract contract, String functionName, Object[] args);

    SolidityContract map(final String abi, byte[] address);

    EthAddress publish(String code);
}


