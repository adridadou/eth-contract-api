package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.*;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.config.ChainId;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListener;
import org.ethereum.solidity.compiler.SolidityCompiler;
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

    public static EthereumFacade forTest(TestConfig config){
        EthereumJTest ethereumj = new EthereumJTest(config);
        EthereumEventHandler ethereumListener = new EthereumEventHandler(ethereumj);
        ethereumListener.onSyncDone(EthereumListener.SyncState.COMPLETE);
        return new Builder(BlockchainConfig.builder()).create(ethereumj, ethereumListener);
    }

    public static class Builder {

        private final BlockchainConfig.Builder configBuilder;

        public Builder(BlockchainConfig.Builder configBuilder) {
            this.configBuilder = configBuilder;
        }

        public BlockchainConfig.Builder extendConfig() {
            return configBuilder;
        }

        public EthereumFacade createReal(){
            GenericConfig.config = configBuilder.build().toString();
            EthereumJReal ethereum = new EthereumJReal(EthereumFactory.createEthereum(GenericConfig.class));
            return create(ethereum, new EthereumEventHandler(ethereum));
        }

        public EthereumFacade create(Ethereumj ethereum, EthereumEventHandler ethereumListener) {
            InputTypeHandler inputTypeHandler = new InputTypeHandler();
            OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
            return new EthereumFacade(new EthereumProxyEthereumJ(ethereum, ethereumListener,inputTypeHandler, outputTypeHandler),inputTypeHandler, outputTypeHandler, SwarmService.from(SwarmService.PUBLIC_HOST), SolidityCompiler.getInstance());
        }
    }
}
