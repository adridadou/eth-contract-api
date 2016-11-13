package org.adridadou.ethereum.values;

import java.util.Arrays;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthExecutionResult that = (EthExecutionResult) o;

        return Arrays.equals(result, that.result);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(result);
    }

    @Override
    public String toString() {
        return "result:" + EthData.of(result).toString();
    }
}
