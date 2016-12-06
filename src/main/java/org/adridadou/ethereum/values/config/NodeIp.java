package org.adridadou.ethereum.values.config;

/**
 * Created by davidroon on 06.12.16.
 * This code is released under Apache 2 license
 */
public class NodeIp {
    public final String ip;

    public NodeIp(String ip) {
        this.ip = ip;
    }

    public static NodeIp ip(final String ip) {
        return new NodeIp(ip);
    }

    @Override
    public String toString() {
        return "\"" + ip + "\"";
    }
}
