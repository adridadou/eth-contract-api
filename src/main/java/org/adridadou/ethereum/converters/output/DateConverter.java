package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class DateConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Date.class.equals(cls);
    }

    @Override
    public Date convert(Object obj, Type genericType) {
        if (obj.getClass().equals(BigInteger.class)) {
            return Date.from(Instant.ofEpochSecond(((BigInteger) obj).intValue()));
        }
        throw new IllegalArgumentException("cannot convert " + obj.getClass().getSimpleName() + " to LocalDate");
    }
}
