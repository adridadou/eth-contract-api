package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.blockchain.BlockchainProxyReal;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.config.ChainId;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacadeProvider {
    public final static ChainId MAIN_CHAIN_ID = ChainId.id(0);
    public final static ChainId ROPSTEN_CHAIN_ID = ChainId.id(3);

    public static Builder forNetwork(final BlockchainConfig.Builder config) {
        return new Builder(config);
    }

    private static class GenericConfig {
        private static String config;

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config));
            return props;
        }
    }

    public static class Builder {

        private final BlockchainConfig.Builder configBuilder;

        public Builder(BlockchainConfig.Builder configBuilder) {
            this.configBuilder = configBuilder;
        }

        public BlockchainConfig.Builder extendConfig() {
            return configBuilder;
        }

        public EthereumFacade create() {
            return create(new OnBlockHandler(), new OnTransactionHandler());
        }

        public EthereumFacade create(OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler) {
            GenericConfig.config = configBuilder.build().toString();
            Ethereum ethereum = EthereumFactory.createEthereum(GenericConfig.class);
            EthereumEventHandler ethereumListener = new EthereumEventHandler(ethereum, onBlockHandler, onTransactionHandler);
            InputTypeHandler inputTypeHandler = new InputTypeHandler();
            OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
            return new EthereumFacade(new BlockchainProxyReal(ethereum, ethereumListener, SwarmService.from(SwarmService.PUBLIC_HOST),inputTypeHandler),inputTypeHandler, outputTypeHandler);
        }
    }
}
