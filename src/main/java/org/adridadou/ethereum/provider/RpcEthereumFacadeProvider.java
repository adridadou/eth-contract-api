package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.blockchain.BlockchainProxyRpc;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class RpcEthereumFacadeProvider {

    public EthereumFacade create(final String url) {
        Web3j web3j = Web3j.build(new HttpService(url));

        EthereumFacade facade = new EthereumFacade(new BlockchainProxyRpc(web3j));

        facade.addInputHandlers(InputTypeHandler.JAVA_INPUT_CONVERTERS);
        facade.addOutputHandlers(OutputTypeHandler.JAVA_OUTPUT_CONVERTERS);

        return facade;
    }
}
