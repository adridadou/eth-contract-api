package org.adridadou.ethereum;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

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

    public String toString() {
        return "0x" + Hex.toHexString(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthData ethData = (EthData) o;

        return Arrays.equals(data, ethData.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
