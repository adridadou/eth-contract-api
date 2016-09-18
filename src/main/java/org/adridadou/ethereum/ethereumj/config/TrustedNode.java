package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * # list of trusted peers from which incoming connections are always accepted
 * trusted = [
 * # Sample entries:
 * # {nodeId = "e437a4836b77ad9d9ffe73ee782ef2614e6d8370fcf62191a6e488276e23717147073a7ce0b444d485fff5a0c34c4577251a7a990cf80d8542e21b95aa8c5e6c"},
 * # {ip = "11.22.33.44"},
 * # {ip = "11.22.33.*"},
 * # {
 * #   nodeId = "e437a4836b77ad9d9ffe73ee782ef2614e6d8370fcf62191a6e488276e23717147073a7ce0b444d485fff5a0c34c4577251a7a990cf80d8542e21b95aa8c5e6c"
 * #   ip = "11.22.33.44"
 * # }
 * ]
 */
public class TrustedNode {
    private final String nodeId;
    private final String ip;

    public TrustedNode(String nodeId, String ip) {
        this.nodeId = nodeId;
        this.ip = ip;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getIp() {
        return ip;
    }
}
