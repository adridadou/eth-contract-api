package org.adridadou.ethereum.converters.output;

import org.adridadou.ethereum.values.EthAddress;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class AddressConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return EthAddress.class.equals(cls);
    }

    @Override
    public EthAddress convert(Object obj, Type type) {
        if (obj.getClass().equals(BigInteger.class)) {
            BigInteger bint = (BigInteger) obj;
            byte[] barray = bint.toByteArray();
            byte[] address = new byte[barray.length - 1];
            System.arraycopy(barray, 1, address, 0, address.length);
            return EthAddress.of(address);
        }
        try {
            return EthAddress.of((byte[]) obj);
        } catch (Exception ex) {
            throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to Address");
        }
    }
}
