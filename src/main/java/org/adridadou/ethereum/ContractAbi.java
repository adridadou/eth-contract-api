package org.adridadou.ethereum;

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
}
