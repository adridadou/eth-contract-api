package org.adridadou.ethereum;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public class EthAddress {
    public final byte[] address;


    public EthAddress(byte[] address) {
        this.address = address;
    }

    public static EthAddress of(byte[] address) {
        return new EthAddress(address);
    }

    public static EthAddress of(final String address) {

        return null;
    }
}
