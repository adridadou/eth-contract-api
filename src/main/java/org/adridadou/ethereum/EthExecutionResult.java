package org.adridadou.ethereum;

/**
 * Created by davidroon on 13.10.16.
 * This code is released under Apache 2 license
 */
public class EthExecutionResult {
    private final byte[] result;

    public EthExecutionResult(byte[] result) {
        this.result = result;
    }

    public byte[] getResult() {
        return result;
    }
}
