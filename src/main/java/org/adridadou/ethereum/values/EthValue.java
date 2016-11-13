package org.adridadou.ethereum.values;


import java.math.BigInteger;

/**
 * Created by davidroon on 06.11.16.
 * This code is released under Apache 2 license
 */
public class EthValue implements Comparable<EthValue> {
    private final BigInteger value;
    private static final BigInteger ETHER_CONVERSION = BigInteger.valueOf(1_000_000_000_000_000_000L);

    public EthValue(BigInteger value) {
        this.value = value;
    }

    public static EthValue ether(final BigInteger value) {
        return new EthValue(value.multiply(ETHER_CONVERSION));
    }

    public static EthValue wei(final int value) {
        return wei(BigInteger.valueOf(value));
    }

    public static EthValue wei(final BigInteger value) {
        return new EthValue(value);
    }

    public BigInteger inWei() {
        return value;
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

        return value != null ? value.equals(ethValue.value) : ethValue.value == null;

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
