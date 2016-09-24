package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public class BootNodeUrl implements BootNodeConfig {
    private final String url;

    public BootNodeUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
