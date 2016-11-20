package org.adridadou.ethereum.converters.output;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 18.11.16.
 * This code is released under Apache 2 license
 */
public class CompletableFutureConverter implements OutputTypeConverter {

    private final OutputTypeHandler handler;

    public CompletableFutureConverter(OutputTypeHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean isOfType(Class<?> cls) {
        return cls.equals(CompletableFuture.class);
    }

    @Override
    public Object convert(Object obj, Type genericType) {
        return handler.getConverter(getGenericType(genericType)).map(converter -> converter.convert(obj, getGenericType(genericType)))
                .orElseThrow(() -> new IllegalArgumentException("no handler founds to convert " + genericType.getTypeName()));
    }

    private Class<?> getGenericType(Type genericType) {
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }
}
