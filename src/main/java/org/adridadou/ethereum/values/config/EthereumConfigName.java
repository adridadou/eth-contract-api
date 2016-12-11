package org.adridadou.ethereum.values.config;

/**
 * Created by davidroon on 06.12.16.
 * This code is released under Apache 2 license
 */
public class EthereumConfigName {
    public final String name;

    public EthereumConfigName(String name) {
        this.name = name;
    }

    public static EthereumConfigName name(final String name) {
        return new EthereumConfigName(name);
    }
}
