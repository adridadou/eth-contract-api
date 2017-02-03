package org.adridadou.ethereum;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.values.*;

import java.math.BigInteger;

/**
 * Created by davidroon on 20.01.17.
 */
public interface EthereumBackend {
    BigInteger getGasPrice();

    EthValue getBalance(EthAddress address);

    boolean addressExists(EthAddress address);

    EthData submit(final EthAccount account, final EthAddress address,final EthValue value, final EthData data, final BigInteger nonce);

    BigInteger estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data);

    BigInteger getNonce(EthAddress currentAddress);

    long getCurrentBlockNumber();

    SmartContractByteCode getCode(EthAddress address);

    EthData executeLocally(final EthAccount account, final EthAddress address, final EthValue value, final EthData data);

    void register(EthereumEventHandler eventHandler);
}
