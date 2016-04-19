package org.adridadou.util;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyTest implements BlockchainProxy {
    private final StandaloneBlockchain blockchain = new StandaloneBlockchain();

    @Override
    public Object[] call(final SolidityContract contract, final String functionName, Object[] args) {
        return contract.callConstFunction(functionName, args);
    }

    @Override
    public SolidityContract map(String abi, byte[] address) {
        return blockchain.createExistingContractFromABI(abi, address);
    }

    @Override
    public EthAddress publish(String code) {
        blockchain.createBlock();
        SolidityContract result = blockchain.submitNewContract(code);
        blockchain.createBlock();
        return EthAddress.of(result.getAddress());
    }


}
