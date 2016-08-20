package org.adridadou.ethereum.converters;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class BooleanHandler implements TypeHandler<Boolean> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Boolean.class.equals(cls) || "boolean".equals(cls.getSimpleName());
    }

    @Override
    public Boolean convert(Object obj) {
        return (Boolean) obj;
    }
}
