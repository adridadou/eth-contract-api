package org.adridadou.ethereum.converters.output;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.reflect.Array.newInstance;

/**
 * Created by davidroon on 18.11.16.
 * This code is released under Apache 2 license
 */
public class ArrayConverter implements OutputTypeConverter {

    private final OutputTypeHandler handler;

    public ArrayConverter(OutputTypeHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.isArray();
    }

    @Override
    public Object convert(Object obj, Type genericType) {
        Object[] arr = (Object[]) obj;

        return handler.getConverter(getGenericType(genericType))
                .map(converter -> Arrays.stream(arr)
                        .map(o -> converter.convert(o, genericType)).collect(Collectors.toList()))
                .orElseThrow(() -> new IllegalArgumentException("no handler founds to convert " + genericType.getTypeName()))
                .toArray((Object[]) newInstance(getGenericType(genericType), arr.length));
    }

    private Class<?> getGenericType(Type genericType) {
        return (Class<?>) genericType;
    }
}
