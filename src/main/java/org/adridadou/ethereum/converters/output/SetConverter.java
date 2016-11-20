package org.adridadou.ethereum.converters.output;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 18.11.16.
 * This code is released under Apache 2 license
 */
public class SetConverter implements OutputTypeConverter {

    private final OutputTypeHandler handler;

    public SetConverter(OutputTypeHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(Set.class);
    }

    @Override
    public Object convert(Object obj, Type genericType) {
        Object[] arr = (Object[]) obj;
        return handler.getConverter(getGenericType(genericType)).map(converter -> Arrays.stream(arr)
                .map(o -> converter.convert(o, getGenericType(genericType)))
                .collect(Collectors.toSet())).orElseThrow(() -> new IllegalArgumentException("no handler founds to convert " + genericType.getTypeName()));
    }

    private Class<?> getGenericType(Type genericType) {
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }
}
