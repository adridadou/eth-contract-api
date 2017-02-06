package org.adridadou.ethereum.rpc.provider;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.rpc.EthereumRPC;
import org.adridadou.ethereum.EthereumProxy;
import org.adridadou.ethereum.rpc.EthereumRpcEventGenerator;
import org.adridadou.ethereum.rpc.Web3JFacade;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.config.ChainId;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacadeRpcProvider {
    public EthereumFacade create(final String url, final ChainId chainId) {
        return create(new Web3JFacade(Web3j.build(new HttpService(url)), new OutputTypeHandler(), chainId));
    }

    public EthereumFacade create(final Web3JFacade web3j) {
        EthereumRPC ethRpc = new EthereumRPC(web3j, new EthereumRpcEventGenerator(web3j));
        InputTypeHandler inputTypeHandler = new InputTypeHandler();
        OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
        return new EthereumFacade(new EthereumProxy(ethRpc, new EthereumEventHandler(), inputTypeHandler, outputTypeHandler), inputTypeHandler, outputTypeHandler, new SwarmService(SwarmService.PUBLIC_HOST),SolidityCompiler.getInstance());
    }
}
