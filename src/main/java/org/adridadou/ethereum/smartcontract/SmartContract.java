package org.adridadou.ethereum.smartcontract;

import org.adridadou.ethereum.values.EthValue;
import org.ethereum.core.CallTransaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 18.08.16.
 * This code is released under Apache 2 license
 */
public interface SmartContract {
    CompletableFuture<Object[]> callFunction(String methodName, Object... arguments);

    CompletableFuture<Object[]> callFunction(String methodName, EthValue value, Object... arguments);

    Object[] callConstFunction(String methodName, Object... arguments);

    List<CallTransaction.Function> getFunctions();
}
