package org.adridadou.ethereum.smartcontract;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.core.CallTransaction.Contract;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class RealSmartContract implements SmartContract {
    private EthAddress address;
    private Contract contract;
    private final Ethereum ethereum;
    private final BlockchainProxy bcProxy;
    private final ECKey sender;

    public RealSmartContract(String abi, Ethereum ethereum, ECKey sender, BlockchainProxy bcProxy) {
        this.contract = new Contract(abi);
        this.ethereum = ethereum;
        this.sender = sender;
        this.bcProxy = bcProxy;
    }

    public List<CallTransaction.Function> getFunctions() {
        return Lists.newArrayList(contract.functions);
    }

    public Object[] callConstFunction(Block callBlock, String functionName, Object... args) {

        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                address.toString(), 0, contract.getByName(functionName), args);
        tx.sign(ECKey.fromPrivate(new byte[32]));

        Repository repository = getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();

        try {
            TransactionExecutor executor = new TransactionExecutor
                    (tx, callBlock.getCoinbase(), repository, getBlockchain().getBlockStore(),
                            getBlockchain().getProgramInvokeFactory(), callBlock)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            return contract.getByName(functionName).decodeResult(executor.getResult().getHReturn());
        } finally {
            repository.rollback();
        }
    }

    public void setAddress(EthAddress address) {
        this.address = address;
    }

    private BlockchainImpl getBlockchain() {
        return (BlockchainImpl) ethereum.getBlockchain();
    }

    private Repository getRepository() {
        return getBlockchain().getRepository();
    }


    public Observable<Object[]> callFunction(String functionName, Object... args) {
        return callFunction(1, functionName, args);
    }

    public Observable<Object[]> callFunction(long value, String functionName, Object... args) {
        CallTransaction.Function func = contract.getByName(functionName);

        if (func == null) {

            throw new EthereumApiException("function " + functionName + " cannot be found. available:" + getAvailableFunctions());
        }
        byte[] functionCallBytes = func.encode(args);
        try {
            return bcProxy.sendTx(value, functionCallBytes, sender)
                    .map(receipt -> contract.getByName(functionName).decodeResult(receipt.getExecutionResult()));
        } catch (InterruptedException e) {
            throw new EthereumApiException(e.getMessage());
        }
    }

    private String getAvailableFunctions() {
        List<String> names = new ArrayList<>();
        for (CallTransaction.Function func : contract.functions) {
            names.add(func.name);
        }
        return names.toString();
    }

    public Object[] callConstFunction(String functionName, Object... args) {
        return callConstFunction(getBlockchain().getBestBlock(), functionName, args);
    }

    public EthAddress getAddress() {
        return address;
    }
}
