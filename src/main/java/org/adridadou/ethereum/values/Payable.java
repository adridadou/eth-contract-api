package org.adridadou.ethereum.values;

import org.adridadou.ethereum.EthereumContractInvocationHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 15.01.17.
 */
public class Payable<T> {

    private final SmartContract contract;
    private final String methodName;
    private final Object[] arguments;
    private final Method method;
    private final EthereumContractInvocationHandler ethereumContractInvocationHandler;

    public Payable(SmartContract contract, String methodName, Object[] arguments, Method method, EthereumContractInvocationHandler ethereumContractInvocationHandler) {

        this.contract = contract;
        this.methodName = methodName;
        this.arguments = arguments;
        this.method = method;
        this.ethereumContractInvocationHandler = ethereumContractInvocationHandler;
    }

    public CompletableFuture<T> with(EthValue value) {
        return (CompletableFuture<T>)contract.callFunction(methodName,value, arguments).thenApply(result -> ethereumContractInvocationHandler.convertResult(result,method));
    }

}
