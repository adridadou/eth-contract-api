package org.adridadou.ethereum;

import org.ethereum.util.blockchain.SolidityContract;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public interface BlockchainProxy {
    SolidityContract map(final String src, EthAddress address);

    SolidityContract mapFromAbi(final String abi, EthAddress address);

    EthAddress publish(String code);

    boolean isSyncDone();

    EthAddress getSenderAddress();
}


