package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public class BootNodeObject implements BootNodeConfig {
    private final String ip;
    private final int port;
    private final String nodeId;

    public BootNodeObject(String ip, int port, String nodeId) {
        this.ip = ip;
        this.port = port;
        this.nodeId = nodeId;
    }
}
