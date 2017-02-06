package org.adridadou.ethereum;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.values.*;

import java.math.BigInteger;

/**
 * Created by davidroon on 20.01.17.
 * This code is released under Apache 2 license
 */
public interface EthereumBackend {
    BigInteger getGasPrice();

    EthValue getBalance(EthAddress address);

    boolean addressExists(EthAddress address);

    EthHash submit(final EthAccount account, final EthAddress address,final EthValue value, final EthData data, final BigInteger nonce, final BigInteger gasLimit);

    BigInteger estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data);

    BigInteger getNonce(EthAddress currentAddress);

    long getCurrentBlockNumber();

    SmartContractByteCode getCode(EthAddress address);

    EthData constantCall(final EthAccount account, final EthAddress address, final EthValue value, final EthData data);

    void register(EthereumEventHandler eventHandler);
}
