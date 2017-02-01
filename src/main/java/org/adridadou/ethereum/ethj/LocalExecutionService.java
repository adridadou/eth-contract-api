package org.adridadou.ethereum.ethj;

import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;

/**
 * Created by davidroon on 30.01.17.
 */
public class LocalExecutionService {
    private final BlockchainImpl blockchain;
    private final ChainId chainId;

    public LocalExecutionService(BlockchainImpl blockchain, ChainId chainId) {
        this.blockchain = blockchain;
        this.chainId = chainId;
    }


    public BigInteger estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data, final BigInteger nonce) {
        Block callBlock = blockchain.getBestBlock();
        Repository repository = getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();
        try {
            TransactionExecutor executor = new TransactionExecutor
                    (createTransaction(account,nonce, BigInteger.ZERO, BigInteger.valueOf(100_000_000_000L),address,value,data), callBlock.getCoinbase(), repository, blockchain.getBlockStore(),
                            blockchain.getProgramInvokeFactory(), callBlock)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();
            if(!executor.getReceipt().isSuccessful()) {
                throw new EthereumApiException(executor.getReceipt().getError());
            }
            long gasUsed = executor.getGasUsed();
            return BigInteger.valueOf(gasUsed);
        } finally {
            repository.rollback();
        }
    }

    public EthData executeLocally(final EthAccount account, final EthAddress address, final EthValue value, final EthData data, final BigInteger nonce) {
        Block callBlock = blockchain.getBestBlock();
        Repository repository = getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();

        try {
            TransactionExecutor executor = new TransactionExecutor
                    (createTransaction(account,nonce, BigInteger.ZERO, BigInteger.valueOf(100_000_000_000L),address,value,data), callBlock.getCoinbase(), repository, blockchain.getBlockStore(),
                            blockchain.getProgramInvokeFactory(), callBlock)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            if(!executor.getReceipt().isSuccessful()) {
                throw new EthereumApiException(executor.getReceipt().getError());
            }
            return EthData.of(executor.getResult().getHReturn());
        } finally {
            repository.rollback();
        }
    }

    private Repository getRepository() {
        return blockchain.getRepository();
    }

    private Transaction createTransaction(EthAccount account, BigInteger nonce, BigInteger gasPrice, BigInteger gas, EthAddress address, EthValue value, EthData data) {
        byte[] nonceBytes = ByteUtil.bigIntegerToBytes(nonce);
        byte[] gasPriceBytes = ByteUtil.bigIntegerToBytes(gasPrice);
        byte[] gasBytes = ByteUtil.bigIntegerToBytes(gas);
        byte[] valueBytes = ByteUtil.bigIntegerToBytes(value.inWei());

        Transaction tx = new Transaction(nonceBytes, gasPriceBytes, gasBytes,
                address.address, valueBytes, data.data, chainId.id);

        tx.sign(account.key);
        return tx;
    }
}
