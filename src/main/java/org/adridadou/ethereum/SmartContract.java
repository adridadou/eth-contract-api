package org.adridadou.ethereum;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.CallTransaction.Contract;

import static org.adridadou.ethereum.values.EthValue.wei;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class SmartContract {
    private final EthAddress address;
    private final EthereumBackend ethereum;
    private final Contract contract;
    private final EthereumProxy proxy;
    private final EthAccount account;

    public SmartContract(Contract contract, EthAccount account, EthAddress address, EthereumProxy proxy, EthereumBackend ethereum) {
        this.contract = contract;
        this.account = account;
        this.proxy = proxy;
        this.address = address;
        this.ethereum = ethereum;
    }

    public List<CallTransaction.Function> getFunctions() {
        return Lists.newArrayList(contract.functions);
    }

    public Object[] callConstFunction(String functionName, EthValue value, Object... args) {
        CallTransaction.Function func = contract.getByName(functionName);
        EthData data = EthData.of(func.encode(args));
        return func.decodeResult(ethereum.constantCall(account,address,value,data).data);
    }

    public CompletableFuture<Object[]> callFunction(String functionName, Object... args) {
        return callFunction(wei(0), functionName, args);
    }

    public CompletableFuture<Object[]> callFunction(String functionName, EthValue value, Object... arguments) {
        return callFunction(value, functionName, arguments);
    }

    public CompletableFuture<Object[]> callFunction(EthValue value, String functionName, Object... args) {
        return Arrays.stream(contract.functions).filter(f-> f.name.equals(functionName) && f.inputs.length == args.length).findFirst().map((func) -> {
            EthData functionCallBytes = EthData.of(func.encode(args));
            return proxy.sendTx(value, functionCallBytes, account, address)
                    .thenApply(receipt -> contract.getByName(functionName).decodeResult(receipt.getResult().data));
        }).orElseThrow(() -> new EthereumApiException("function " + functionName + " cannot be found. available:" + getAvailableFunctions()));
    }

    private String getAvailableFunctions() {
        return Arrays.stream(contract.functions)
                .map(c -> c.name)
                .collect(Collectors.toList()).toString();
    }

    public EthAddress getAddress() {
        return address;
    }
}
