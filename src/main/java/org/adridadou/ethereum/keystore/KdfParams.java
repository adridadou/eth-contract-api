package org.adridadou.ethereum.keystore;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class KdfParams {
    private Integer c;
    private Integer dklen;
    private String prf;
    private String salt;

    public KdfParams() {
        this(null, null, null, null);
    }

    public KdfParams(Integer c, Integer dklen, String prf, String salt) {
        this.c = c;
        this.dklen = dklen;
        this.prf = prf;
        this.salt = salt;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
        this.c = c;
    }

    public Integer getDklen() {
        return dklen;
    }

    public void setDklen(Integer dklen) {
        this.dklen = dklen;
    }

    public String getPrf() {
        return prf;
    }

    public void setPrf(String prf) {
        this.prf = prf;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
