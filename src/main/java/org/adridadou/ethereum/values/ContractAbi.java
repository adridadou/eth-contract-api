package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public class ContractAbi {
    private final String abi;

    public ContractAbi(String abi) {
        this.abi = abi;
    }

    public String getAbi() {
        return abi;
    }

    public static ContractAbi of(final String abi) {
        return new ContractAbi(abi);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContractAbi that = (ContractAbi) o;

        return abi != null ? abi.equals(that.abi) : that.abi == null;

    }

    @Override
    public int hashCode() {
        return abi != null ? abi.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "abi:" + abi;
    }
}
