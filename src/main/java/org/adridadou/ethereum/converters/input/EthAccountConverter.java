package org.adridadou.ethereum.converters.input;

import org.adridadou.ethereum.values.EthAccount;

/**
 * Created by davidroon on 13.11.16.
 * This code is released under Apache 2 license
 */
public class EthAccountConverter implements InputTypeConverter<byte[]> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(EthAccount.class);
    }

    @Override
    public byte[] convert(Object obj) {
        return ((EthAccount) obj).getAddress().address;
    }
}
