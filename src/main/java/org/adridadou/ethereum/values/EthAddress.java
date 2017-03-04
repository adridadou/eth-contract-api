package org.adridadou.ethereum.values;

import java.util.Arrays;

import com.google.common.base.Preconditions;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public class EthAddress {
    public static final int MAX_ADDRESS_SIZE = 32;
    public final byte[] address;


    private EthAddress(byte[] address) {
        Preconditions.checkArgument(address.length <= MAX_ADDRESS_SIZE, "byte array of the address cannot be bigger than 32.value:" + Hex.toHexString(address));
        this.address = address;
    }

    public static EthAddress of(byte[] address) {
        if(address == null) {
            return EthAddress.empty();
        }
        return new EthAddress(trimLeft(address));
    }

    public static EthAddress of(ECKey key) {
        return new EthAddress(trimLeft(key.getAddress()));
    }

    private static byte[] trimLeft(byte[] address) {
        int firstNonZeroPos = 0;
        while (firstNonZeroPos < address.length && address[firstNonZeroPos] == 0) {
            firstNonZeroPos++;
        }

        byte[] newAddress = new byte[address.length - firstNonZeroPos];
        System.arraycopy(address, firstNonZeroPos, newAddress, 0, address.length - firstNonZeroPos);

        return newAddress;
    }

    public static EthAddress of(final String address) {
        if(address==null) 
        	return empty();
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        EthAddress that = (EthAddress) o;

        return Arrays.equals(address, that.address);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }

    public static EthAddress empty() {
        return EthAddress.of(ByteUtil.EMPTY_BYTE_ARRAY);
    }

    public boolean isEmpty() {
        return Arrays.equals(this.address, ByteUtil.EMPTY_BYTE_ARRAY);
    }
}
