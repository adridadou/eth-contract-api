package org.adridadou.ethereum.smartcontract;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.blockchain.BlockchainProxyRpc;
import org.adridadou.ethereum.blockchain.Web3JFacade;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.CallTransaction.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class SmartContractRpc implements SmartContract {
    private EthAddress address;
    private Contract contract;
    private final Web3JFacade web3j;
    private final BlockchainProxyRpc bcProxy;
    private final EthAccount sender;

    public SmartContractRpc(String abi, Web3JFacade web3j, EthAccount sender, EthAddress address, BlockchainProxyRpc bcProxy) {
        this.contract = new Contract(abi);
        this.web3j = web3j;
        this.sender = sender;
        this.bcProxy = bcProxy;
        this.address = address;
    }

    public List<CallTransaction.Function> getFunctions() {
        return Lists.newArrayList(contract.functions);
    }

    public Object[] callConstFunction(String functionName, Object... args) {

        return Optional.ofNullable(contract.getByName(functionName))
                .map(func -> {
                    EthData result = web3j.constantCall(sender, address, EthData.of(func.encode(args)));
                    return func.decodeResult(result.data);
                }).orElseThrow(() -> new EthereumApiException("function " + functionName + " cannot be found. available:" + getAvailableFunctions()));
    }

    public CompletableFuture<Object[]> callFunction(String functionName, Object... args) {
        return callFunction(EthValue.wei(0), functionName, args);
    }

    public CompletableFuture<Object[]> callFunction(EthValue value, String functionName, Object... args) {
        return Optional.ofNullable(contract.getByName(functionName))
                .map(func -> bcProxy.sendTx(value, EthData.of(func.encode(args)), sender, address)
                .thenApply(receipt -> Optional.ofNullable(receipt.getResult())
                        .map(result -> contract.getByName(functionName).decodeResult(result)).orElse(null)))
                .orElseThrow(() -> new EthereumApiException("function " + functionName + " cannot be found. available:" + getAvailableFunctions()));
    }

    private String getAvailableFunctions() {
        List<String> names = new ArrayList<>();
        for (CallTransaction.Function func : contract.functions) {
            names.add(func.name);
        }
        return names.toString();
    }

    public EthAddress getAddress() {
        return address;
    }
}
