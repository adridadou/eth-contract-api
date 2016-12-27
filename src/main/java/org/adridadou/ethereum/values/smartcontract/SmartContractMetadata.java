package org.adridadou.ethereum.values.smartcontract;

import org.adridadou.ethereum.values.ContractAbi;

/**
 * Created by davidroon on 22.12.16.
 * This code is released under Apache 2 license
 */
public class SmartContractMetadata {
    final private ContractAbi abi;

    public SmartContractMetadata(String abi) {
        this.abi = new ContractAbi(abi);
    }

    public ContractAbi getAbi() {
        return abi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartContractMetadata that = (SmartContractMetadata) o;

        return abi != null ? abi.equals(that.abi) : that.abi == null;
    }

    @Override
    public int hashCode() {
        return abi != null ? abi.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SmartContractMetadata{" +
                "abi=" + abi +
                '}';
    }
}
