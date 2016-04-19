package org.adridadou.ethereum;

import org.ethereum.util.blockchain.SolidityContract;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    Object[] call(SolidityContract contract, String functionName, Object[] args);

    SolidityContract map(final String abi, byte[] address);

    EthAddress publish(String code);
}


