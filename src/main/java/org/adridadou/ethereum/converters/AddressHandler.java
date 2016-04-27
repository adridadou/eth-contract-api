package org.adridadou.ethereum.converters;

import org.adridadou.ethereum.EthAddress;

import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class AddressHandler implements TypeHandler<EthAddress> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return EthAddress.class.equals(cls);
    }

    @Override
    public EthAddress convert(Object obj) {
        if (obj.getClass().equals(BigInteger.class)) {
            BigInteger bint = (BigInteger) obj;
            byte[] barray = bint.toByteArray();
            byte[] address = new byte[barray.length - 1];
            System.arraycopy(barray, 1, address, 0, address.length);
            return EthAddress.of(address);
        }
        throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to Integer");
    }
}
