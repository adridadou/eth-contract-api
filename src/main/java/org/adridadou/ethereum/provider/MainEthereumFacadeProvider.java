package org.adridadou.ethereum.provider;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.keystore.Keystore;
import org.adridadou.ethereum.keystore.SecureKey;
import org.ethereum.crypto.ECKey;
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
        return new EthereumFacade(new EthereumContractInvocationHandler(proxy), proxy);
    }

    @Override
    public ECKey getKey(String id, String password) throws Exception {

        return Keystore.fromKeystore(new File(getKeystoreFolderPath() + id), password);
    }

    @Override
    public List<SecureKey> listAvailableKeys() {
        return javaslang.collection.List
                .of(new File(getKeystoreFolderPath()).listFiles())
                .filter(File::isFile)
                .map(SecureKey::new)
                .toJavaList();
    }

    private String getKeystoreFolderPath() {
        String homeDir = System.getProperty("user.home");
        return homeDir + "/Library/Ethereum/keystore/";
    }
}
