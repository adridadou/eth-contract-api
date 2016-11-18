package org.adridadou.ethereum.converters.output;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class BooleanConverter implements OutputTypeConverter<Boolean> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Boolean.class.equals(cls) || "boolean".equals(cls.getSimpleName());
    }

    @Override
    public Boolean convert(Object obj, Class<?> cls) {
        return (Boolean) obj;
    }
}
