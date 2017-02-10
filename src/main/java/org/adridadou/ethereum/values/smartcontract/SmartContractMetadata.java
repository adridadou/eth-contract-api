package org.adridadou.ethereum.values.smartcontract;

import org.adridadou.ethereum.values.ContractAbi;

/**
 * Created by davidroon on 22.12.16.
 * This code is released under Apache 2 license
 */
public class SmartContractMetadata {
    private final ContractAbi abi;

    public SmartContractMetadata(String abi) {
        this.abi = new ContractAbi(abi);
    }

    public ContractAbi getAbi() {
        return abi;
    }

    @Override
    public String toString() {
        return "SmartContractMetadata{" +
                "abi=" + abi +
                '}';
    }
}
