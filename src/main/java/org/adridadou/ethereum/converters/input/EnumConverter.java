package org.adridadou.ethereum.converters.input;

/**
 * Created by davidroon on 26.12.16.
 * This code is released under Apache 2 license
 */
public class EnumConverter implements InputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.isEnum();
    }

    @Override
    public Integer convert(Object obj) {
        return ((Enum) obj).ordinal();
    }
}
