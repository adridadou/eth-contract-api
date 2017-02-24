package org.adridadou.ethereum.rpc;

import org.adridadou.ethereum.EthereumBackend;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.values.*;
import org.ethereum.core.Transaction;

import java.math.BigInteger;

/**
 * Created by davidroon on 20.01.17.
 * This code is released under Apache 2 license
 */
public class EthereumRPC implements EthereumBackend {
    private final Web3JFacade web3JFacade;
    private final EthereumRpcEventGenerator ethereumRpcEventGenerator;

    public EthereumRPC(Web3JFacade web3JFacade, EthereumRpcEventGenerator ethereumRpcEventGenerator) {
        this.web3JFacade = web3JFacade;
        this.ethereumRpcEventGenerator = ethereumRpcEventGenerator;
    }

    @Override
    public BigInteger getGasPrice() {
        return web3JFacade.getGasPrice();
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return EthValue.wei(web3JFacade.getBalance(address).getBalance());
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return false;
    }

    @Override
    public EthHash submit(EthAccount account, EthAddress address, EthValue value, EthData data, BigInteger nonce, BigInteger gasLimit) {
        Transaction tx = createTransaction(account, nonce, getGasPrice(), gasLimit, address, value, data);
        web3JFacade.sendTransaction(EthData.of(tx.getEncoded()));
        return EthHash.of(tx.getHash());
    }

    private Transaction createTransaction(EthAccount account, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, EthAddress address, EthValue value, EthData data) {
        Transaction tx = web3JFacade.createTransaction(nonce, gasPrice, gasLimit, address, value, data);
        tx.sign(account.key);
        return tx;
    }

    @Override
    public BigInteger estimateGas(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return web3JFacade.estimateGas(account, address, value, data);
    }

    @Override
    public BigInteger getNonce(EthAddress currentAddress) {
        return web3JFacade.getTransactionCount(currentAddress);
    }

    @Override
    public long getCurrentBlockNumber() {
        return web3JFacade.getCurrentBlockNumber();
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return web3JFacade.getCode(address);
    }

    @Override
    public EthData constantCall(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return web3JFacade.constantCall(account, address, data);
    }

    @Override
    public void register(EthereumEventHandler eventHandler) {
        ethereumRpcEventGenerator.addListener(eventHandler);
    }
}
