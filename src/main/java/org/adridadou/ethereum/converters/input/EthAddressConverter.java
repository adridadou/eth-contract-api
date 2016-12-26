package org.adridadou.ethereum.converters.input;

import org.adridadou.ethereum.values.EthAddress;

/**
 * Created by davidroon on 13.11.16.
 * This code is released under Apache 2 license
 */
public class EthAddressConverter implements InputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(EthAddress.class);
    }

    @Override
    public byte[] convert(Object obj) {
        return ((EthAddress) obj).address;
    }
}
