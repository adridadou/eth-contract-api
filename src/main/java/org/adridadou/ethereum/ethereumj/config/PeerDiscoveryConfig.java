package org.adridadou.ethereum.ethereumj.config;

import java.util.Set;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * <p>
 * * peer.discovery = {
 * <p>
 * # if peer discovery is off
 * # the peer window will show
 * # only what was retrieved by active
 * # peers [true/false]
 * enabled = true
 * <p>
 * # number of workers that
 * # test the peers for being
 * # online [1..10]
 * workers = 8
 * <p>
 * # List of the seed peers to start
 * # the search for online peers
 * # values: [ip:port, ip:port, ip:port ...]
 * ip.list = [
 * "54.94.239.50:30303",
 * "52.16.188.185:30303",
 * "frontier-2.ether.camp:30303",
 * "frontier-3.ether.camp:30303",
 * "frontier-4.ether.camp:30303"
 * ]
 * <p>
 * # indicates if the discovered nodes and their reputations
 * # are stored in DB and persisted between VM restarts
 * persist = true
 * <p>
 * # the period in seconds with which the discovery
 * # tries to reconnect to successful nodes
 * # 0 means the nodes are not reconnected
 * touchPeriod = 600
 * <p>
 * # the maximum nuber of nodes to reconnect to
 * # -1 for unlimited
 * touchMaxNodes = 100
 * <p>
 * # external IP/hostname which is reported as our host during discovery
 * # if not set, the service http://checkip.amazonaws.com is used
 * # the last resort is to get the peer.bind.ip address
 * external.ip = null
 * <p>
 * # Local network adapter IP to which
 * # the discovery UDP socket is bound
 * # e.g: 192.168.1.104
 * #
 * # if the value is empty it will be retrieved
 * # by punching to some known address e.g: www.google.com
 * bind.ip = ""
 * <p>
 * # indicates whether the discovery will include own home node
 * # within the list of neighbor nodes
 * public.home.node = true
 * }
 */
public class PeerDiscoveryConfig {
    private final boolean includeHomeNode;
    private final IpAddress ipAddress;
    private final IpAddress externalIpAddress;
    private final int touchMaxNodes;
    private final int touchPeriod;
    private final boolean persist;
    private final Set<HostAddress> peersList;
    private final int workers;
    private final boolean enabled;

    public PeerDiscoveryConfig(boolean includeHomeNode, IpAddress ipAddress, IpAddress externalIpAddress, int touchMaxNodes, int touchPeriod, boolean persist, Set<HostAddress> peersList, int workers, boolean enabled) {
        this.includeHomeNode = includeHomeNode;
        this.ipAddress = ipAddress;
        this.externalIpAddress = externalIpAddress;
        this.touchMaxNodes = touchMaxNodes;
        this.touchPeriod = touchPeriod;
        this.persist = persist;
        this.peersList = peersList;
        this.workers = workers;
        this.enabled = enabled;
    }

    public boolean isIncludeHomeNode() {
        return includeHomeNode;
    }

    public IpAddress getIpAddress() {
        return ipAddress;
    }

    public IpAddress getExternalIpAddress() {
        return externalIpAddress;
    }

    public int getTouchMaxNodes() {
        return touchMaxNodes;
    }

    public int getTouchPeriod() {
        return touchPeriod;
    }

    public boolean isPersist() {
        return persist;
    }

    public Set<HostAddress> getPeersList() {
        return peersList;
    }

    public int getWorkers() {
        return workers;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
