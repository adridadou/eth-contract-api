package org.adridadou.ethereum.converters;

import java.lang.reflect.Method;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface TypeHandler<T> {
    boolean isOfType(Method method);

    T convert(Object[] result);
}
