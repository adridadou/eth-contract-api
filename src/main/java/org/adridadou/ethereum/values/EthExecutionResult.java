package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 13.10.16.
 * This code is released under Apache 2 license
 */
public class EthExecutionResult {
    private final EthData result;

    public EthExecutionResult(EthData result) {
        this.result = result;
    }

    public EthData getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthExecutionResult that = (EthExecutionResult) o;

        return result != null ? result.equals(that.result) : that.result == null;
    }

    @Override
    public int hashCode() {
        return result != null ? result.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "EthExecutionResult{" +
                "result=" + result +
                '}';
    }
}
