package org.adridadou.ethereum.converters.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by davidroon on 17.11.16.
 * This code is released under Apache 2 license
 */
public class OutputTypeHandler {

    public static final List<OutputTypeConverter<?>> JAVA_OUTPUT_CONVERTERS = ImmutableList.<OutputTypeConverter<?>>builder().add(
            new IntegerConverter(),
            new LongConverter(),
            new StringConverter(),
            new BooleanConverter(),
            new AddressConverter(),
            new VoidConverter(),
            new EnumConverter()
    ).build();

    private final List<OutputTypeConverter<? extends Object>> outputConverters = new ArrayList<>();

    public void addConverters(final OutputTypeConverter<?>... converters) {
        addConverters(Lists.newArrayList(converters));
    }

    public void addConverters(final Collection<OutputTypeConverter<?>> converters) {
        outputConverters.addAll(converters);
    }

    public Optional<OutputTypeConverter<?>> getConverter(final Class<?> cls) {
        return outputConverters.stream().filter(converter -> converter.isOfType(cls)).findFirst();
    }

    public Object convert(final Object arg) {
        return getConverter(arg.getClass()).map(converter -> converter.convert(arg, arg.getClass())).orElse(null);
    }
}
