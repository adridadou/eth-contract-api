package org.adridadou.ethereum.smartcontract;

import org.adridadou.ethereum.EthAddress;
import org.adridadou.ethereum.EthereumListenerImpl;
import org.ethereum.core.*;
import org.ethereum.core.CallTransaction.Contract;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.SolidityStorage;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class SolidityContractImpl implements SolidityContract {
    private EthAddress address;
    private Contract contract;
    private final Ethereum ethereum;
    private final EthereumListenerImpl ethereumListener;
    private final ECKey sender;

    public SolidityContractImpl(String abi, Ethereum ethereum, EthereumListenerImpl ethereumListener, ECKey sender) {
        this.ethereumListener = ethereumListener;
        contract = new Contract(abi);
        this.ethereum = ethereum;
        this.sender = sender;
    }

    @Override
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

    @Override
    public void call(byte[] callData) {
        // for this we need cleaner separation of EasyBlockchain to
        // Abstract and Solidity specific
        throw new UnsupportedOperationException();
    }

    private CompletableFuture<TransactionReceipt> sendTx(EthAddress receiveAddress, byte[] data) throws InterruptedException {
        BigInteger nonce = getRepository().getNonce(sender.getAddress());
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                receiveAddress.address,
                ByteUtil.longToBytesNoLeadZeroes(1),
                data);
        tx.sign(sender);
        ethereum.submitTransaction(tx);
        return ethereumListener.registerTx(tx.getHash());
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

    @Override
    public byte[] getAddress() {
        if (address == null) {
            throw new RuntimeException("Contract address will be assigned only after block inclusion. Call createBlock() first.");
        }
        return address.address;
    }

    @Override
    public SolidityCallResult callFunction(String functionName, Object... args) {
        return callFunction(0, functionName, args);
    }

    @Override
    public SolidityCallResult callFunction(long value, String functionName, Object... args) {
        CallTransaction.Function inc = contract.getByName(functionName);
        byte[] functionCallBytes = inc.encode(args);
        try {
            sendTx(address, functionCallBytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object[] callConstFunction(String functionName, Object... args) {
        return callConstFunction(getBlockchain().getBestBlock(), functionName, args);
    }

    @Override
    public SolidityStorage getStorage() {
        return new SolidityStorageImpl(getAddress(), getBlockchain());
    }

    @Override
    public String getABI() {
        throw new UnsupportedOperationException("ABI is not saved");
    }

    @Override
    public String getBinary() {
        throw new UnsupportedOperationException("Binary is not saved");
    }
}
