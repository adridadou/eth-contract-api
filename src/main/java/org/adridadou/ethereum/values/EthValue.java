package org.adridadou.ethereum.values;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Created by davidroon on 06.11.16.
 * This code is released under Apache 2 license
 */
public class EthValue implements Comparable<EthValue> {
    private final BigDecimal value;
    private final static BigDecimal ETHER_CONVERSION = BigDecimal.valueOf(1_000_000_000_000_000_000L);

    public EthValue(BigInteger value) {
        this.value = new BigDecimal(value);
    }

    public BigInteger inWei() {
        return value.toBigInteger();
    }

    public BigDecimal inEth() {
        return value
                .divide(ETHER_CONVERSION, BigDecimal.ROUND_FLOOR);
    }

    public EthValue plus(EthValue value) {
        return new EthValue(this.value.add(value.value).toBigInteger());
    }

    public EthValue minus(EthValue value) {
        return new EthValue(this.value.subtract(value.value).toBigInteger());
    }

    public static EthValue ether(final BigInteger value) {
        return wei(value.multiply(ETHER_CONVERSION.toBigInteger()));
    }

    public static EthValue ether(final Double value) {
        return ether(BigDecimal.valueOf(value));
    }

    public static EthValue ether(final BigDecimal value) {
        return wei(ETHER_CONVERSION.multiply(value).toBigInteger());
    }

    public static EthValue ether(final long value) {
        return ether(BigInteger.valueOf(value));
    }

    public static EthValue wei(final int value) {
        return wei(BigInteger.valueOf(value));
    }

    public static EthValue wei(final long value) {
        return wei(BigInteger.valueOf(value));
    }

    public static EthValue wei(final BigInteger value) {
        return new EthValue(value);
    }

    @Override
    public int compareTo(EthValue o) {
        return value.compareTo(o.value);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && Objects.equals(value, ((EthValue)o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value + " Wei";
    }
}
