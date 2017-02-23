package org.adridadou.ethereum.values;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public class EthHash {
    public final byte[] data;

    private EthHash(byte[] data) {
        this.data = data;
    }

    public static EthHash of(byte[] data) {
        return new EthHash(data);
    }

    public static EthHash of(final String data) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EthHash ethData = (EthHash) o;

        return Arrays.equals(data, ethData.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public static EthHash empty() {
        return EthHash.of(new byte[0]);
    }
}
