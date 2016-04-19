package org.adridadou.util;

import org.adridadou.config.EthereumConfig;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidroon on 09.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumConfigTest implements EthereumConfig {

    private final Map<Class<?>, byte[]> addresses = new HashMap<>();

    @Override
    public Class getUserSpringConfig() {
        return null;
    }

    @Override
    public byte[] getAddress(Class<?> contractInterface) {
        return addresses.get(contractInterface);
    }

    public void setAddress(Class<?> contractInterface, byte[] address) {
        addresses.put(contractInterface, address);
    }
}
