package org.adridadou.ethereum.converters.future;

import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.ethereum.SmartContract;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 26.02.17.
 * This code is released under Apache 2 license
 */
public interface FutureConverter {
    <T> Object convert(final CompletableFuture<T> future);

    boolean isFutureType(Class cls);

    boolean isPayableType(Class cls);

    Object getPayable(SmartContract smartContract, String methodName, Object[] arguments, Method method, EthereumContractInvocationHandler ethereumContractInvocationHandler);
}
