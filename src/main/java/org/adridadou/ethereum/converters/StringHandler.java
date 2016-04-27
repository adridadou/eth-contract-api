package org.adridadou.ethereum.converters;

import java.lang.reflect.Method;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class StringHandler implements TypeHandler<String> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return String.class.equals(cls);
    }

    @Override
    public String convert(Object obj) {
        return obj.toString();

    }
}
