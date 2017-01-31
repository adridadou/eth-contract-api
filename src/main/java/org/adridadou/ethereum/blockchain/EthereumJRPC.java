package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.values.*;
import org.ethereum.core.Transaction;

import java.math.BigInteger;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumJRPC implements Ethereumj{
    private final Web3JFacade web3JFacade;
    private final EthereumRpcEventGenerator ethereumRpcEventGenerator;

    public EthereumJRPC(Web3JFacade web3JFacade, EthereumRpcEventGenerator ethereumRpcEventGenerator) {
        this.web3JFacade = web3JFacade;
        this.ethereumRpcEventGenerator = ethereumRpcEventGenerator;
    }

    @Override
    public void close() {

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
    public EthData submit(EthAccount account, EthAddress address, EthValue value, EthData data, BigInteger nonce) {
        Transaction tx = createTransaction(account, nonce, getGasPrice(), estimateGas(account, address, value, data), address, value, data);
        web3JFacade.sendTransaction(EthData.of(tx.getEncodedRaw()));
        return EthData.of(tx.getHash());
    }

    private Transaction createTransaction(EthAccount account, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, EthAddress address, EthValue value, EthData data) {
        Transaction tx = web3JFacade.createTransaction(nonce, gasPrice, gasLimit, address, value, data);
        tx.sign(account.key);
        return tx;
    }

    @Override
    public void addListener(EthereumEventHandler ethereumEventHandler) {
        ethereumRpcEventGenerator.addListener(ethereumEventHandler);
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
    public EthData executeLocally(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return web3JFacade.constantCall(account, address, data);
    }

}
