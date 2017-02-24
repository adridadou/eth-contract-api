package org.adridadou.ethereum;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.ethj.BlockchainConfig;
import org.adridadou.ethereum.ethj.EthereumReal;
import org.adridadou.ethereum.ethj.EthereumTest;
import org.adridadou.ethereum.ethj.TestConfig;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.rpc.EthereumRPC;
import org.adridadou.ethereum.rpc.EthereumRpcEventGenerator;
import org.adridadou.ethereum.rpc.Web3JFacade;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.ethereum.values.config.InfuraKey;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.springframework.context.annotation.Bean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacadeProvider {
    public static final ChainId MAIN_CHAIN_ID = ChainId.id(0);
    public static final ChainId ROPSTEN_CHAIN_ID = ChainId.id(3);
    public static final ChainId ETHER_CAMP_CHAIN_ID = ChainId.id(161);

    private EthereumFacadeProvider() {}

    public static Builder forNetwork(final BlockchainConfig config) {
        return new Builder(config);
    }

    public static EthereumFacade forTest(TestConfig config){
        EthereumTest ethereumj = new EthereumTest(config);
        EthereumEventHandler ethereumListener = new EthereumEventHandler();
        ethereumListener.onReady();
        return new Builder(BlockchainConfig.builder()).create(ethereumj, ethereumListener);
    }

    public static EthereumFacade forRemoteNode(final String url, final ChainId chainId) {
        Web3JFacade web3j = new Web3JFacade(Web3j.build(new HttpService(url)), new OutputTypeHandler(), chainId);
        EthereumRPC ethRpc = new EthereumRPC(web3j, new EthereumRpcEventGenerator(web3j));
        InputTypeHandler inputTypeHandler = new InputTypeHandler();
        OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
        return new EthereumFacade(new EthereumProxy(ethRpc, new EthereumEventHandler(), inputTypeHandler, outputTypeHandler), inputTypeHandler, outputTypeHandler, new SwarmService(SwarmService.PUBLIC_HOST),SolidityCompiler.getInstance());
    }

    public static InfuraBuilder forInfura(final InfuraKey key)  {
        return new InfuraBuilder(key);
    }

    public static class InfuraBuilder {
        private final InfuraKey key;

        public InfuraBuilder(InfuraKey key) {
            this.key = key;
        }

        public EthereumFacade createMain() {
            return forRemoteNode("https://main.infura.io/" + key.key, EthereumFacadeProvider.MAIN_CHAIN_ID);
        }

        public EthereumFacade createRopsten() {
            return forRemoteNode("https://ropsten.infura.io/" + key.key, EthereumFacadeProvider.MAIN_CHAIN_ID);
        }
    }

    public static class Builder {

        private final BlockchainConfig configBuilder;

        public Builder(BlockchainConfig configBuilder) {
            this.configBuilder = configBuilder;
        }

        public BlockchainConfig extendConfig() {
            return configBuilder;
        }

        public EthereumFacade create(){
            GenericConfig.config = configBuilder.toString();
            EthereumReal ethereum = new EthereumReal(EthereumFactory.createEthereum(GenericConfig.class));
            return create(ethereum, new EthereumEventHandler());
        }

        public EthereumFacade create(EthereumBackend ethereum, EthereumEventHandler ethereumListener) {
            InputTypeHandler inputTypeHandler = new InputTypeHandler();
            OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
            return new EthereumFacade(new EthereumProxy(ethereum, ethereumListener,inputTypeHandler, outputTypeHandler),inputTypeHandler, outputTypeHandler, SwarmService.from(SwarmService.PUBLIC_HOST), SolidityCompiler.getInstance());
        }
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
}
