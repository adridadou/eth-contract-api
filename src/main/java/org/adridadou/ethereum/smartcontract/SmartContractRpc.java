package org.adridadou.ethereum.smartcontract;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.*;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.CallTransaction.Contract;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.io.IOException;
import java.math.BigInteger;
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
    private final Web3j web3j;
    private final BlockchainProxyRpc bcProxy;
    private final EthAccount sender;
    private final EthAddress senderAddress;

    public SmartContractRpc(String abi, Web3j web3j, EthAccount sender, EthAddress address, BlockchainProxyRpc bcProxy) {
        this.contract = new Contract(abi);
        this.web3j = web3j;
        this.sender = sender;
        this.bcProxy = bcProxy;
        this.address = address;
        this.senderAddress = sender.getAddress();
    }

    public List<CallTransaction.Function> getFunctions() {
        return Lists.newArrayList(contract.functions);
    }

    public Object[] callConstFunction(String functionName, Object... args) {

        return Optional.ofNullable(contract.getByName(functionName))
                .map(func -> {
                    try {
                        EthCall result = web3j.ethCall(new Transaction(
                                senderAddress.withLeading0x(),
                                BigInteger.ZERO,
                                BigInteger.ZERO,
                                BigInteger.valueOf(1_000_000_000),
                                address.withLeading0x(), BigInteger.ZERO,
                                EthData.of(func.encode(args)).toString()
                        ), DefaultBlockParameter.valueOf("latest")).send();

                        if (result.hasError()) {
                            throw new EthereumApiException(result.getError().getMessage());
                        }
                        return func.decodeResult(EthData.of(result.getValue()).data);
                    } catch (IOException e) {
                        throw new EthereumApiException("error while calling a constant function");
                    }
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
