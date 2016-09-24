package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public enum VmTraceDumpStyle {
    PRETTY("pretty"), STANDARD_PLUS("standard+");

    public final String KEY;

    VmTraceDumpStyle(String s) {
        this.KEY = s;
    }
}
