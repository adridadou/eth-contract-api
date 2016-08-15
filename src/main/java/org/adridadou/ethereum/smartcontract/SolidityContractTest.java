package org.adridadou.ethereum.smartcontract;

import org.adridadou.ethereum.EthAddress;
import org.ethereum.core.Block;
import org.ethereum.util.blockchain.SolidityCallResult;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 15.08.16.
 * This code is released under Apache 2 license
 */
public class SolidityContractTest extends SolidityContract {
    private final org.ethereum.util.blockchain.SolidityContract contract;

    public SolidityContractTest(org.ethereum.util.blockchain.SolidityContract contract) {
        this.contract = contract;
    }

    @Override
    public Object[] callConstFunction(Block callBlock, String functionName, Object... args) {
        return contract.callConstFunction(callBlock, functionName, args);
    }

    @Override
    public CompletableFuture<Object[]> callFunction(String functionName, Object... args) {
        return convertResult(contract.callFunction(functionName, args));
    }

    @Override
    public CompletableFuture<Object[]> callFunction(long value, String functionName, Object... args) {
        return convertResult(contract.callFunction(value, functionName, args));
    }

    @Override
    public Object[] callConstFunction(String functionName, Object... args) {
        return contract.callConstFunction(functionName, args);
    }

    @Override
    public EthAddress getAddress() {
        return EthAddress.of(contract.getAddress());
    }


    private CompletableFuture<Object[]> convertResult(SolidityCallResult result) {
        CompletableFuture<Object[]> future = new CompletableFuture<>();
        future.complete(result.getReturnValues());
        return future;
    }
}
