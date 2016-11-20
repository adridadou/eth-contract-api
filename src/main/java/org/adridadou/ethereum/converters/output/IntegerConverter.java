package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class IntegerConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Integer.class.equals(cls) || "int".equals(cls.getSimpleName());
    }

    @Override
    public Integer convert(Object obj, Type genericType) {
        if (obj.getClass().equals(BigInteger.class)) {
            return ((BigInteger) obj).intValue();
        }
        throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to Integer");
    }
}
