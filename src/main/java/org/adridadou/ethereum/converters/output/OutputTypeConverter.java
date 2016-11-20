package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public interface OutputTypeConverter {
    boolean isOfType(Class<?> cls);

    Object convert(Object obj, Type genericType);
}
