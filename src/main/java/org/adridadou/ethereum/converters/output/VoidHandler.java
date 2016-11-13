package org.adridadou.ethereum.converters.output;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class VoidHandler implements OutputTypeHandler<Void> {
    @Override
    public boolean isOfType(Class<?> cls) {
        return Void.class.equals(cls) || "void".equals(cls.getSimpleName());
    }

    @Override
    public Void convert(Object obj, Class<?> cls) {
        return null;
    }
}
