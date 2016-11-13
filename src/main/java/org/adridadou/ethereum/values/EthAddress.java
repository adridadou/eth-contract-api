package org.adridadou.ethereum.values;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

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
        return new EthAddress(trimLeft(address));
    }

    public static EthAddress of(ECKey key) {
        return new EthAddress(trimLeft(key.getAddress()));
    }

    private static byte[] trimLeft(byte[] address) {
        int firstNonZeroPos = 0;
        while (firstNonZeroPos < address.length && address[firstNonZeroPos] == 0) firstNonZeroPos++;

        byte[] newAddress = new byte[address.length - firstNonZeroPos];
        System.arraycopy(address, firstNonZeroPos, newAddress, 0, address.length - firstNonZeroPos);

        return newAddress;
    }

    public static EthAddress of(final String address) {
        if (address != null && address.startsWith("0x")) {
            return of(Hex.decode(address.substring(2)));
        }
        return of(Hex.decode(address));
    }

    public String toString() {
        return Hex.toHexString(address);
    }

    public String withLeading0x() {
        return "0x" + this.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthAddress that = (EthAddress) o;

        return Arrays.equals(address, that.address);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }
}
