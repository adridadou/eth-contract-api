package org.adridadou.ethereum;

import org.spongycastle.util.encoders.Hex;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public class EthAddress {
    public final byte[] address;


    private EthAddress(byte[] address) {
        this.address = address;
    }

    public static EthAddress of(byte[] address) {
        return new EthAddress(address);
    }

    public static EthAddress of(final String address) {
        return of(Hex.decode(address));
    }

    public String toString() {
        return Hex.toHexString(address);
    }
}
