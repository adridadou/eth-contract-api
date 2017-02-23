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
    public String toString() {
        return "EthExecutionResult{" +
                "result=" + result +
                '}';
    }
}
