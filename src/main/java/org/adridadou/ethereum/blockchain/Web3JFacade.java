package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.SmartContractByteCode;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.vm.LogInfo;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Numeric;
import rx.Observable;

import java.io.IOError;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by davidroon on 19.11.16.
 * This code is released under Apache 2 license
 */
public class Web3JFacade {
    private final Web3j web3j;
    private final OutputTypeHandler outputTypeHandler;

    public Web3JFacade(final Web3j web3j, OutputTypeHandler outputTypeHandler) {
        this.web3j = web3j;
        this.outputTypeHandler = outputTypeHandler;
    }

    public EthData constantCall(final EthAccount account, final EthAddress address, final EthData data) {
        try {
            return EthData.of(handleError(web3j.ethCall(new Transaction(
                    account.getAddress().withLeading0x(),
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    BigInteger.valueOf(1_000_000_000),
                    address.withLeading0x(),
                    BigInteger.ZERO,
                    data.toString()
            ), DefaultBlockParameterName.LATEST).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public TransactionReceipt getTransactionReceipt(final EthData transactionHash) {
        try {
            return handleError(web3j.ethGetTransactionReceipt(transactionHash.withLeading0x()).send());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public BigInteger getTransactionCount(EthAddress address) {
        try {
            return Numeric.decodeQuantity(handleError(web3j.ethGetTransactionCount(address.withLeading0x(), DefaultBlockParameterName.LATEST).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public BigInteger estimateGas(EthAccount sender, EthData data) {
        try {
            return Numeric.decodeQuantity(handleError(web3j.ethEstimateGas(Transaction.createEthCallTransaction(sender.getAddress().withLeading0x(), data.toString())).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public BigInteger getGasPrice() {
        try {
            return Numeric.decodeQuantity(handleError(web3j.ethGasPrice().send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public EthData sendTransaction(final EthData rawTransaction) {
        try {
            return EthData.of(handleError(web3j.ethSendRawTransaction(rawTransaction.withLeading0x()).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public EthGetBalance getBalance(EthAddress address) {
        try {
            return web3j.ethGetBalance(address.withLeading0x(), DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }


    private <S, T extends Response<S>> S handleError(final T response) {
        if (response.hasError()) {
            throw new EthereumApiException(response.getError().getMessage());
        }
        return response.getResult();
    }

    public SmartContractByteCode getCode(EthAddress address) {
        try {
            return SmartContractByteCode.of(web3j.ethGetCode(address.withLeading0x(), DefaultBlockParameterName.LATEST).send().getCode());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public <T> Observable<T> event(final EthAddress address,final String eventName, final CallTransaction.Contract contract, Class<T> cls) {
        return web3j.ethLogObservable(new EthFilter(DefaultBlockParameterName.EARLIEST,DefaultBlockParameterName.LATEST,address.withLeading0x()))
                .map(log -> {
                    LogInfo logInfo = new LogInfo(address.address, new ArrayList<>(), EthData.of(log.getData()).data);
                    return contract.parseEvent(logInfo);
                }).filter(invocation -> eventName.equals(invocation.function.name))
                .map(invocation -> outputTypeHandler.convertSpecificType(invocation.args, cls));
    }

    public OutputTypeHandler getOutputTypeHandler() {
        return outputTypeHandler;
    }
}
