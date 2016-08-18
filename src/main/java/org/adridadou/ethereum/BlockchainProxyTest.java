package org.adridadou.ethereum;

import org.adridadou.ethereum.smartcontract.RealSmartContract;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.TestSmartContract;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import rx.Observable;

import java.math.BigInteger;

import static org.ethereum.config.blockchain.FrontierConfig.*;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyTest implements BlockchainProxy {
    private final StandaloneBlockchain blockchain;

    public BlockchainProxyTest() {

        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        blockchain = new StandaloneBlockchain();
        blockchain.withAutoblock(true);
    }

    @Override
    public SmartContract map(String src, String contractName, EthAddress address, ECKey sender) {
        return new TestSmartContract(blockchain.createExistingContractFromSrc(src, contractName, address.address));

    }

    @Override
    public SmartContract mapFromAbi(String abi, EthAddress address, ECKey sender) {
        return new TestSmartContract(blockchain.createExistingContractFromABI(abi, address.address));
    }

    @Override
    public Observable<EthAddress> publish(String code, String contractName, ECKey sender) {
        return Observable.just(EthAddress.of(blockchain.submitNewContract(code).getAddress()));
    }

    @Override
    public Observable<TransactionReceipt> sendTx(long value, byte[] data, ECKey sender) throws InterruptedException {
        return null;
    }
}
