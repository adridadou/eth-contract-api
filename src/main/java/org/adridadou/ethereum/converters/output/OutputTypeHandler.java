package org.adridadou.ethereum.converters.output;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface OutputTypeHandler<T> {
    boolean isOfType(Class<?> cls);

    T convert(Object obj, Class<?> cls);
}
