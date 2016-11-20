package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class VoidConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Void.class.equals(cls) || "void".equals(cls.getSimpleName());
    }

    @Override
    public String convert(Object obj, Type type) {
        return "";
    }
}
