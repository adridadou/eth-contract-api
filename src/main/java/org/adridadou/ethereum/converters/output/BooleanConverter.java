package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class BooleanConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Boolean.class.equals(cls) || "boolean".equals(cls.getSimpleName());
    }

    @Override
    public Boolean convert(Object obj, Type type) {
        return (Boolean) obj;
    }
}
