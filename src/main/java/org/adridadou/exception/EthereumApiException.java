package org.adridadou.exception;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumApiException extends RuntimeException {
    public EthereumApiException(String s) {
        super(s);
    }

    public EthereumApiException(String s, Throwable t) {
        super(s, t);
    }
}
