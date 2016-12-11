package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.blockchain.BlockchainProxyReal;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.values.config.ChainId;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class GenericEthereumFacadeProvider {

    public final static ChainId MAIN_CHAIN_ID = ChainId.id(0);
    public final static ChainId ROPSTEN_CHAIN_ID = ChainId.id(3);

    private static class GenericConfig {
        static String config;


        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    public EthereumFacade create(final BlockchainConfig config) {
        return create(new OnBlockHandler(), new OnTransactionHandler(), config);
    }

    public EthereumFacade create(OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler, final BlockchainConfig config) {
        GenericConfig.config = config.toString();
        Ethereum ethereum = EthereumFactory.createEthereum(GenericConfig.class);
        EthereumEventHandler ethereumListener = new EthereumEventHandler(ethereum, onBlockHandler, onTransactionHandler);
        ethereum.init();

        return new EthereumFacade(new BlockchainProxyReal(ethereum, ethereumListener));
    }
}
