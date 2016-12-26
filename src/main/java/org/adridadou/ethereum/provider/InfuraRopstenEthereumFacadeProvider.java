package org.adridadou.ethereum.provider;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.values.config.*;
import org.adridadou.exception.EthereumApiException;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class InfuraRopstenEthereumFacadeProvider {

    public EthereumFacade create(final InfuraKey key) {
        return new GenericRpcEthereumFacadeProvider().create("https://ropsten.infura.io/" + key.key, GenericEthereumFacadeProvider.ROPSTEN_CHAIN_ID);
    }
}
