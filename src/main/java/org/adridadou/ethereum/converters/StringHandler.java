package org.adridadou.ethereum.converters;

import java.lang.reflect.Method;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class StringHandler implements TypeHandler<String> {
    @Override
    public boolean isOfType(Method method) {
        return String.class.equals(method.getReturnType());
    }

    @Override
    public String convert(Object[] result) {
        return result[0].toString();

    }
}
