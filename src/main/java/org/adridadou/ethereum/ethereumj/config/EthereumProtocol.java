package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public enum EthereumProtocol {
    ETH("eth"), SHH("shh"), BZZ("bzz");

    public final String ID;

    EthereumProtocol(String id) {
        this.ID = id;
    }
}
