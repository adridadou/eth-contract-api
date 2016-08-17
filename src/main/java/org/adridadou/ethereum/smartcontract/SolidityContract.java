package org.adridadou.ethereum.smartcontract;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.core.CallTransaction.Contract;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class SolidityContract {
    private EthAddress address;
    private Contract contract;
    private final Ethereum ethereum;
    private final BlockchainProxy bcProxy;
    private final ECKey sender;

    SolidityContract() {
        this.contract = null;
        this.ethereum = null;
        this.sender = null;
        this.bcProxy = null;
    }

    public SolidityContract(String abi, Ethereum ethereum, ECKey sender, BlockchainProxy bcProxy) {
        this.contract = new Contract(abi);
        this.ethereum = ethereum;
        this.sender = sender;
        this.bcProxy = bcProxy;
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


    public CompletableFuture<Object[]> callFunction(String functionName, Object... args) {
        return callFunction(1, functionName, args);
    }

    public CompletableFuture<Object[]> callFunction(long value, String functionName, Object... args) {
        CallTransaction.Function inc = contract.getByName(functionName);
        byte[] functionCallBytes = inc.encode(args);
        try {
            return bcProxy.sendTx(value, functionCallBytes, sender)
                    .thenApply(receipt -> contract.getByName(functionName).decodeResult(receipt.getExecutionResult()));
        } catch (InterruptedException e) {
            throw new EthereumApiException(e.getMessage());
        }
    }

    public Object[] callConstFunction(String functionName, Object... args) {
        return callConstFunction(getBlockchain().getBestBlock(), functionName, args);
    }

    public EthAddress getAddress() {
        return address;
    }
}
