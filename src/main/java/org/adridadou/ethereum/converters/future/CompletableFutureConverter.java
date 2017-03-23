package org.adridadou.ethereum.converters.future;

import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.ethereum.SmartContract;
import org.adridadou.ethereum.values.Payable;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 26.02.17.
 * This code is released under Apache 2 license
 */
public class CompletableFutureConverter implements FutureConverter {
    @Override
    public CompletableFuture convert(CompletableFuture future) {
        return future;
    }

    @Override
    public boolean isFutureType(Class cls) {
        return CompletableFuture.class.equals(cls);
    }

    @Override
    public boolean isPayableType(Class cls) {
        return Payable.class.equals(cls);
    }

    @Override
    public Payable getPayable(SmartContract smartContract, String methodName, Object[] arguments, Method method, EthereumContractInvocationHandler ethereumContractInvocationHandler) {
        return new Payable(smartContract,methodName,arguments, method, ethereumContractInvocationHandler);
    }
}
