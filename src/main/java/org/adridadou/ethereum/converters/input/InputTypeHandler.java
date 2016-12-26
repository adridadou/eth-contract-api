package org.adridadou.ethereum.converters.input;

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
public class InputTypeHandler {
    public static final List<InputTypeConverter> JAVA_INPUT_CONVERTERS = ImmutableList.<InputTypeConverter>builder().add(
            new EthAddressConverter(),
            new EthAccountConverter(),
            new EthDataConverter(),
            new EthValueConverter(),
            new EnumConverter()
    ).build();


    public InputTypeHandler() {
        addConverters(JAVA_INPUT_CONVERTERS);
    }

    private final List<InputTypeConverter> inputConverters = new ArrayList<>();

    public void addConverters(final InputTypeConverter... converters) {
        addConverters(Lists.newArrayList(converters));
    }

    public void addConverters(final Collection<InputTypeConverter> converters) {
        inputConverters.addAll(converters);
    }


    public Optional<InputTypeConverter> getConverter(final Class<?> cls) {
        return inputConverters.stream().filter(converter -> converter.isOfType(cls)).findFirst();
    }

    public Object convert(final Object arg) {
        return getConverter(arg.getClass()).map(converter -> converter.convert(arg))
                .orElse(arg);
    }
}
