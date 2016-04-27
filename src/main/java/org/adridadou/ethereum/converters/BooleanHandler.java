package org.adridadou.ethereum.converters;

import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class BooleanHandler implements TypeHandler<Boolean> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Boolean.class.equals(cls) || cls.getSimpleName().equals("boolean");
    }

    @Override
    public Boolean convert(Object obj) {
        return (Boolean) obj;
    }
}
