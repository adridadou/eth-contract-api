package org.adridadou.ethereum.converters;

import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class BooleanHandler implements TypeHandler<Boolean> {
    @Override
    public boolean isOfType(Method method) {
        return Boolean.class.equals(method.getReturnType()) || method.getReturnType().getSimpleName().equals("boolean");
    }

    @Override
    public Boolean convert(Object[] result) {
        return (Boolean) result[0];
    }
}
