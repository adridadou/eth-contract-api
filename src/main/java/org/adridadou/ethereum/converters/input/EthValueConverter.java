package org.adridadou.ethereum.converters.input;

import org.adridadou.ethereum.values.EthValue;

import java.math.BigInteger;

/**
 * Created by davidroon on 13.11.16.
 * This code is released under Apache 2 license
 */
public class EthValueConverter implements InputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(EthValue.class);
    }

    @Override
    public BigInteger convert(Object obj) {
        return ((EthValue) obj).inWei();
    }
}
