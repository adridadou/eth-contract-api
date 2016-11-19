package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.exception.EthereumApiException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Numeric;

import java.io.IOError;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by davidroon on 19.11.16.
 * This code is released under Apache 2 license
 */
public class Web3JFacade {
    private final Web3j web3j;

    public Web3JFacade(final Web3j web3j) {
        this.web3j = web3j;
    }

    public EthData constantCall(final EthAccount sender, final EthAddress address, final EthData data) {
        try {
            return EthData.of(handleError(web3j.ethCall(new Transaction(
                    sender.getAddress().withLeading0x(),
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    BigInteger.valueOf(1_000_000_000),
                    address.withLeading0x(), BigInteger.ZERO,
                    data.toString()
            ), DefaultBlockParameter.valueOf("latest")).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public TransactionReceipt getTransactionReceipt(final EthData transactionHash) {
        try {
            return handleError(web3j.ethGetTransactionReceipt(transactionHash.toString()).send());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public BigInteger getTransactionCount(EthAccount sender) {
        try {
            return Numeric.decodeQuantity(handleError(web3j.ethGetTransactionCount(sender.getAddress().withLeading0x(), DefaultBlockParameterName.LATEST).send()));
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
            return web3j.ethGetBalance(address.withLeading0x(), DefaultBlockParameter.valueOf("latest")).send();
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
}
