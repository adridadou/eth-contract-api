package org.adridadou.ethereum.converters.input;

import org.adridadou.ethereum.values.EthData;

/**
 * Created by davidroon on 13.11.16.
 * This code is released under Apache 2 license
 */
public class EthDataHandler implements InputTypeHandler<byte[]> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(EthData.class);
    }

    @Override
    public byte[] convert(Object obj) {
        return ((EthData) obj).data;
    }
}
