package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;

import java.io.File;
import java.util.List;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class MainEthereumFacadeProvider implements EthereumFacadeProvider {

    @Override
    public EthereumFacade create() {
        Ethereum ethereum = EthereumFactory.createEthereum();
        EthereumListenerImpl ethereumListener = new EthereumListenerImpl(ethereum);
        ethereum.init();

        BlockchainProxy proxy = new BlockchainProxyImpl(ethereum, ethereumListener);
        return new EthereumFacade(new EthereumContractInvocationHandler(proxy), proxy, this);
    }

    @Override
    public SecureKey getKey(String id) throws Exception {
        return new FileSecureKey(new File(getKeystoreFolderPath() + id));
    }

    @Override
    public List<? extends SecureKey> listAvailableKeys() {
        return javaslang.collection.List
                .of(new File(getKeystoreFolderPath()).listFiles())
                .filter(File::isFile)
                .map(FileSecureKey::new)
                .toJavaList();
    }

    private String getKeystoreFolderPath() {
        String homeDir = System.getProperty("user.home");
        return homeDir + "/Library/Ethereum/keystore/";
    }
}
