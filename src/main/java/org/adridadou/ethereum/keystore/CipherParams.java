package org.adridadou.ethereum.keystore;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class CipherParams {
    private String iv;

    public CipherParams() {
        this(null);
    }

    public CipherParams(String iv) {
        this.iv = iv;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
