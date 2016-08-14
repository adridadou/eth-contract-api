package org.adridadou.ethereum.smartcontract;

import org.adridadou.ethereum.EthAddress;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Repository;
import org.ethereum.util.blockchain.SolidityStorage;
import org.ethereum.vm.DataWord;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
class SolidityStorageImpl implements SolidityStorage {
    private final EthAddress contractAddr;
    private final BlockchainImpl blockchain;

    SolidityStorageImpl(EthAddress contractAddr, BlockchainImpl blockchain) {
        this.contractAddr = contractAddr;
        this.blockchain = blockchain;
    }

    private Repository getRepository() {
        return blockchain.getRepository();
    }

    @Override
    public byte[] getStorageSlot(long slot) {
        return getStorageSlot(new DataWord(slot).getData());
    }

    @Override
    public byte[] getStorageSlot(byte[] slot) {
        DataWord ret = getRepository().getContractDetails(contractAddr.address).get(new DataWord(slot));
        return ret.getData();
    }
}
