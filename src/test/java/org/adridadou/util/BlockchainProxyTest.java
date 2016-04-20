package org.adridadou.util;

import org.adridadou.ethereum.BlockchainProxy;
import org.adridadou.ethereum.EthAddress;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyTest implements BlockchainProxy {
    private final StandaloneBlockchain blockchain;

    public BlockchainProxyTest() {
        SystemProperties.CONFIG.setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        blockchain = new StandaloneBlockchain();
        blockchain.withAutoblock(true);
    }

    @Override
    public SolidityContract map(String src, byte[] address) {
        return blockchain.createExistingContractFromSrc(src, address);
    }

    @Override
    public EthAddress publish(String code) {
        SolidityContract result = blockchain.submitNewContract(code);
        return EthAddress.of(result.getAddress());
    }


}
