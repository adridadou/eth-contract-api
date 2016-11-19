package org.adridadou.ethereum.values;


import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by davidroon on 06.11.16.
 * This code is released under Apache 2 license
 */
public class EthValue implements Comparable<EthValue> {
    private final BigDecimal value;
    private static final BigDecimal ETHER_CONVERSION = BigDecimal.valueOf(1_000_000_000_000_000_000L);

    public EthValue(BigInteger value) {
        this.value = new BigDecimal(value);
    }

    public static EthValue ether(final BigInteger value) {
        return new EthValue(value.multiply(ETHER_CONVERSION.toBigInteger()));
    }

    public static EthValue wei(final int value) {
        return wei(BigInteger.valueOf(value));
    }

    public static EthValue wei(final BigInteger value) {
        return new EthValue(value);
    }

    public BigInteger inWei() {
        return value.toBigInteger();
    }

    public BigDecimal inEth() {
        return value
                .divide(ETHER_CONVERSION, BigDecimal.ROUND_FLOOR);
    }

    @Override
    public int compareTo(EthValue o) {
        return value.compareTo(o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthValue ethValue = (EthValue) o;

        return value.equals(ethValue.value);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return value + " Wei";
    }
}
