package org.adridadou.ethereum.provider;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.blockchain.BlockchainConfig;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.ethereum.values.config.DatabaseDirectory;
import org.adridadou.ethereum.values.config.EthereumConfigName;
import org.adridadou.ethereum.values.config.GenesisPath;
import org.adridadou.ethereum.values.config.NodeIp;
import org.adridadou.exception.EthereumApiException;
import org.web3j.crypto.WalletUtils;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class RopstenEthereumFacadeProvider {

    public EthereumFacade create() {
        return create(new OnBlockHandler(), new OnTransactionHandler());
    }


    public EthereumFacade create(OnBlockHandler onBlockHandler, OnTransactionHandler onTransactionHandler) {

        return new GenericEthereumFacadeProvider().create(onBlockHandler, onTransactionHandler, BlockchainConfig.builder()
                .addIp(NodeIp.ip("94.242.229.4:40404"))
                .addIp(NodeIp.ip("94.242.229.203:30303"))
                .networkId(GenericEthereumFacadeProvider.ROPSTEN_CHAIN_ID)
                .eip8(true)
                .genesis(GenesisPath.path("ropsten.json"))
                .configName(EthereumConfigName.name("ropsten"))
                .dbDirectory(DatabaseDirectory.db("database-ropsten"))
                .build());
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
