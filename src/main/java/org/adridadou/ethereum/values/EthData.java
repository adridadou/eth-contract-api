package org.adridadou.ethereum.values;

import org.spongycastle.util.encoders.Hex;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public class EthData {
    public final byte[] data;

    private EthData(byte[] data) {
        this.data = data;
    }

    public static EthData of(byte[] data) {
        return new EthData(data);
    }

    public static EthData of(final String data) {
        if (data != null && data.startsWith("0x")) {
            return of(Hex.decode(data.substring(2)));
        }
        return of(Hex.decode(data));
    }

    public String withLeading0x() {
        return "0x" + this.toString();
    }

    public String toString() {
        return Hex.toHexString(data);
    }

    public static EthData empty() {
        return EthData.of(new byte[0]);
    }
}
