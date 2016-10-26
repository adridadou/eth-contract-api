package org.adridadou.ethereum.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.adridadou.ethereum.BlockchainProxyRpc;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.exception.EthereumApiException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */

public class InfuraEthereumFacadeProvider implements EthereumFacadeProvider {

    public EthereumFacade create(final String url) {

        Web3j web3j = Web3j.build(new HttpService(url));
        return new EthereumFacade(new BlockchainProxyRpc(web3j));
    }

    @Override
    public EthereumFacade create() {
        return create("http://localhost:8545/");
    }

    @Override
    public SecureKey getKey(String id) throws Exception {
        File[] files = new File(getKeystoreFolderPath()).listFiles();

        return Lists.newArrayList(Preconditions.checkNotNull(files, "the folder " + getKeystoreFolderPath() + " cannot be found"))
                .stream().filter(file -> id.equals(file.getName()))
                .findFirst().map(FileSecureKey::new)
                .orElseThrow(() -> new EthereumApiException("the file " + id + " could not be found"));
    }

    @Override
    public List<? extends SecureKey> listAvailableKeys() {
        File[] files = Optional.ofNullable(new File(getKeystoreFolderPath()).listFiles()).orElseThrow(() -> new EthereumApiException("cannot find the folder " + getKeystoreFolderPath()));
        return Lists.newArrayList(files).stream()
                .filter(File::isFile)
                .map(FileSecureKey::new)
                .collect(Collectors.toList());
    }

    private String getKeystoreFolderPath() {
        String homeDir = System.getProperty("user.home");
        return homeDir + "/Library/Ethereum/keystore/";
    }
}
