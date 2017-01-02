package org.adridadou.ethereum.converters.input;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by davidroon on 13.11.16.
 * This code is released under Apache 2 license
 */
public class DateConverter implements InputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(Date.class);
    }

    @Override
    public BigInteger convert(Object obj) {
        return BigInteger.valueOf(((Date) obj).toInstant().getEpochSecond());
    }
}
