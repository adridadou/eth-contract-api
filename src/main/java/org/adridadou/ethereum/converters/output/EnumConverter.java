package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by davidroon on 11.11.16.
 * This code is released under Apache 2 license
 */
public class EnumConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.isEnum();
    }

    @Override
    public Enum convert(Object obj, Type type) {
        if (obj.getClass().equals(BigInteger.class)) {
            return ((Class<? extends Enum>) type).getEnumConstants()[((BigInteger) obj).intValue()];
        }
        throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to Enum");
    }
}
