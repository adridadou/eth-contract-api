package org.adridadou.ethereum.values.config;

/**
 * Created by davidroon on 11.12.16.
 * This code is released under Apache 2 license
 */
public class InfuraKey {
    public final String key;

    public InfuraKey(String key) {
        this.key = key;
    }

    public static InfuraKey of(String key) {
        return new InfuraKey(key);
    }

    @Override
    public String toString() {
        return "key=" + key;
    }
}
