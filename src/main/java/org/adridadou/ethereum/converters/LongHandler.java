package org.adridadou.ethereum.converters;

import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class LongHandler implements TypeHandler<Long> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Long.class.equals(cls) || cls.getSimpleName().equals("long");
    }

    @Override
    public Long convert(Object obj) {
        if (obj.getClass().equals(BigInteger.class)) {
            return ((BigInteger) obj).longValue();
        }
        throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to Long");
    }
}
