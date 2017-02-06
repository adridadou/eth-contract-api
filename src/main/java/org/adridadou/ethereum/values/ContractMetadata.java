package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 08.01.17.
 * This code is released under Apache 2 license
 */
public class ContractMetadata {
    private final String value;

    public ContractMetadata(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
