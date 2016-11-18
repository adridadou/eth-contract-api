package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class LongConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Long.class.equals(cls) || "long".equals(cls.getSimpleName());
    }

    @Override
    public Long convert(Object obj, Type type) {
        if (obj.getClass().equals(BigInteger.class)) {
            return ((BigInteger) obj).longValue();
        }
        throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to Long");
    }
}
