package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.blockchain.BlockchainProxyRpc;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.Web3JFacade;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class RpcEthereumFacadeProvider {
    public EthereumFacade create(final String url, final Byte chainId) {
        return create(new Web3JFacade(Web3j.build(new HttpService(url))), chainId);
    }

    public EthereumFacade create(final Web3JFacade web3j, final Byte chainId) {
        return new EthereumFacade(new BlockchainProxyRpc(web3j, chainId));
    }
}
