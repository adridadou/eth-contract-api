package org.adridadou.config;

import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;

import javax.inject.Inject;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumProvider {

    private final EthereumConfig config;

    @Inject
    public EthereumProvider(EthereumConfig config) {
        this.config = config;
    }

    public Ethereum create() {
        final Ethereum ethereum = EthereumFactory.createEthereum(config.getUserSpringConfig());
        ethereum.init();
        return ethereum;
    }
}
