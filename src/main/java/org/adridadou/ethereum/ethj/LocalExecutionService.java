package org.adridadou.ethereum.ethj;

import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;

import java.math.BigInteger;

/**
 * Created by davidroon on 30.01.17.
 * This code is released under Apache 2 license
 */
public class LocalExecutionService {
    public static final long GAS_LIMIT_FOR_LOCAL_EXECUTION = 100_000_000_000L;
    private final BlockchainImpl blockchain;

    public LocalExecutionService(BlockchainImpl blockchain) {
        this.blockchain = blockchain;
    }


    public BigInteger estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        TransactionExecutor execution = execute(account, address, value, data);
        return BigInteger.valueOf(execution.getGasUsed());
    }

    public EthData executeLocally(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        TransactionExecutor execution = execute(account, address, value, data);
        return EthData.of(execution.getResult().getHReturn());
    }

    private TransactionExecutor execute(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        Block callBlock = blockchain.getBestBlock();
        Repository repository = getRepository().getSnapshotTo(callBlock.getStateRoot()).startTracking();
        try {
            Transaction tx = createTransaction(account, BigInteger.ZERO, BigInteger.ZERO, address, value, data);
            TransactionExecutor executor = new TransactionExecutor(tx, callBlock.getCoinbase(), repository, blockchain.getBlockStore(), blockchain.getProgramInvokeFactory(), callBlock).setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            if (!executor.getReceipt().isSuccessful()) {
                throw new EthereumApiException(executor.getReceipt().getError());
            }
            return executor;
        } finally {
            repository.rollback();
        }
    }

    private Repository getRepository() {
        return blockchain.getRepository();
    }

    private Transaction createTransaction(EthAccount account, BigInteger nonce, BigInteger gasPrice, EthAddress address, EthValue value, EthData data) {
        Transaction tx = CallTransaction.createRawTransaction(nonce.longValue(), gasPrice.longValue(), GAS_LIMIT_FOR_LOCAL_EXECUTION, address.toString(), value.inWei().longValue(), data.data);
        tx.sign(getKey(account));
        return tx;
    }

    private ECKey getKey(EthAccount account) {
        return ECKey.fromPrivate(account.getPrivateKey());
    }
}
