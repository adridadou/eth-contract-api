package org.adridadou.ethereum.converters.output;


import java.lang.reflect.Type;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class StringConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return String.class.equals(cls);
    }

    @Override
    public String convert(Object obj, Type type) {
        return obj.toString();

    }
}
