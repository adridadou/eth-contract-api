package org.adridadou.ethereum.converters.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.adridadou.exception.EthereumApiException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by davidroon on 17.11.16.
 * This code is released under Apache 2 license
 */
public class OutputTypeHandler {

    public static final List<OutputTypeConverter> JAVA_OUTPUT_CONVERTERS = ImmutableList.<OutputTypeConverter>builder().add(
            new IntegerConverter(),
            new LongConverter(),
            new StringConverter(),
            new BooleanConverter(),
            new AddressConverter(),
            new VoidConverter(),
            new EnumConverter(),
            new DateConverter(),
            new BigIntegerConverter()
    ).build();

    private final List<OutputTypeConverter> outputConverters = new ArrayList<>();

    public OutputTypeHandler() {
        addConverters(JAVA_OUTPUT_CONVERTERS);
        addConverters(
                new ListConverter(this),
                new ArrayConverter(this),
                new CompletableFutureConverter(this),
                new PayableConverter(this),
                new SetConverter(this));
    }

    public void addConverters(final OutputTypeConverter... converters) {
        addConverters(Lists.newArrayList(converters));
    }

    public void addConverters(final Collection<OutputTypeConverter> converters) {
        outputConverters.addAll(converters);
    }

    public Optional<OutputTypeConverter> getConverter(final Class<?> cls) {
        return outputConverters.stream().filter(converter -> converter.isOfType(cls)).findFirst();
    }

    public <T> T convertSpecificType(Object[] result, Class<T> returnType) {
        Object[] params = new Object[result.length];

        Constructor constr = lookForNonEmptyConstructor(returnType, result);

        for (int i = 0; i < result.length; i++) {
            params[i] = convertResult(result[i], constr.getParameterTypes()[i], constr.getGenericParameterTypes()[i]);
        }

        try {
            return (T) constr.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new EthereumApiException("error while converting to a specific type", e);
        }
    }

    private Constructor lookForNonEmptyConstructor(Class<?> returnType, Object[] result) {
        for (Constructor constructor : returnType.getConstructors()) {
            if (constructor.getParameterCount() > 0) {
                if (constructor.getParameterCount() != result.length) {
                    throw new IllegalArgumentException("the number of arguments don't match for type " + returnType.getSimpleName() + ". Constructor has " + constructor.getParameterCount() + " and result has " + result.length);
                }
                return constructor;
            }
        }
        throw new IllegalArgumentException("no constructor with arguments found! for type " + returnType.getSimpleName());
    }

    public Object convertResult(Object result, Class<?> returnType, Type genericType) {
        return getConverter(returnType)
                .map(converter -> converter.convert(result, returnType.isArray() ? returnType.getComponentType() : genericType))
                .orElseGet(() -> convertSpecificType(new Object[]{result}, returnType));
    }
}
