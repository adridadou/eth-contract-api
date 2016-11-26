package org.adridadou.ethereum.provider;

import com.google.common.collect.Lists;
import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.blockchain.BlockchainProxyReal;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class GenericEthereumFacadeProvider {

    public final static byte MAIN_CHAIN_ID = 0;
    public final static byte ROPSTEN_CHAIN_ID = 3;

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

    public SecureKey getKey(final String id) {
        return listAvailableKeys().stream().filter(file -> file.getName().equals(id)).findFirst().orElseThrow(() -> {
            String names = listAvailableKeys().stream().map(FileSecureKey::getName).reduce((aggregate, name) -> aggregate + "," + name).orElse("");
            return new EthereumApiException("could not find the keyfile " + id + " available:" + names);
        });
    }

    private String getKeystoreFolderPath() {
        return WalletUtils.getTestnetKeyDirectory();
    }

    public List<FileSecureKey> listAvailableKeys() {
        File[] files = Optional.ofNullable(new File(getKeystoreFolderPath()).listFiles()).orElseThrow(() -> new EthereumApiException("cannot find the folder " + getKeystoreFolderPath()));
        return Lists.newArrayList(files).stream()
                .filter(File::isFile)
                .map(FileSecureKey::new)
                .collect(Collectors.toList());
    }
}
