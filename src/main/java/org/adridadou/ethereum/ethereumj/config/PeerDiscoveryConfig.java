package org.adridadou.ethereum.ethereumj.config;

import java.util.Set;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 *
 * * peer.discovery = {
 *
 * # if peer discovery is off
 * # the peer window will show
 * # only what was retrieved by active
 * # peers [true/false]
 * enabled = true
 *
 * # number of workers that
 * # test the peers for being
 * # online [1..10]
 * workers = 8
 *
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
 *
 * # indicates if the discovered nodes and their reputations
 * # are stored in DB and persisted between VM restarts
 * persist = true
 *
 * # the period in seconds with which the discovery
 * # tries to reconnect to successful nodes
 * # 0 means the nodes are not reconnected
 * touchPeriod = 600
 *
 * # the maximum nuber of nodes to reconnect to
 * # -1 for unlimited
 * touchMaxNodes = 100
 *
 * # external IP/hostname which is reported as our host during discovery
 * # if not set, the service http://checkip.amazonaws.com is used
 * # the last resort is to get the peer.bind.ip address
 * external.ip = null
 *
 * # Local network adapter IP to which
 * # the discovery UDP socket is bound
 * # e.g: 192.168.1.104
 * #
 * # if the value is empty it will be retrieved
 * # by punching to some known address e.g: www.google.com
 * bind.ip = ""
 *
 * # indicates whether the discovery will include own home node
 * # within the list of neighbor nodes
 * public.home.node = true
 * }
 */
public class PeerDiscoveryConfig {
    private final Boolean includeHomeNode;
    private final IpAddress ipAddress;
    private final IpAddress externalIpAddress;
    private final Integer touchMaxNodes;
    private final Integer touchPeriod;
    private final Boolean persist;
    private final Set<HostAddress> peersList;
    private final Integer workers;
    private final Boolean enabled;

    public PeerDiscoveryConfig(Boolean includeHomeNode, IpAddress ipAddress, IpAddress externalIpAddress, Integer touchMaxNodes, Integer touchPeriod, Boolean persist, Set<HostAddress> peersList, Integer workers, Boolean enabled) {
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

    @Override
    public String toString() {
        return "PeerDiscoveryConfig{" +
                "includeHomeNode=" + includeHomeNode +
                "ipAddress=" + ipAddress +
                "externalIpAddress=" + externalIpAddress +
                "touchMaxNodes=" + touchMaxNodes +
                "touchPeriod=" + touchPeriod +
                "persist=" + persist +
                "peersList=" + peersList +
                "workers=" + workers +
                "enabled=" + enabled +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean includeHomeNode;
        private IpAddress ipAddress;
        private IpAddress externalIpAddress;
        private Integer touchMaxNodes;
        private Integer touchPeriod;
        private Boolean persist;
        private Set<HostAddress> peersList;
        private Integer workers;
        private Boolean enabled;

        public Builder includeHomeNode(boolean includeHomeNode) {
            this.includeHomeNode = includeHomeNode;
            return this;
        }

        public Builder ipAddress(IpAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder externalIpAddress(IpAddress externalIpAddress) {
            this.externalIpAddress = externalIpAddress;
            return this;
        }

        public Builder touchMaxNodes(int touchMaxNodes) {
            this.touchMaxNodes = touchMaxNodes;
            return this;
        }

        public Builder touchPeriod(int touchPeriod) {
            this.touchPeriod = touchPeriod;
            return this;
        }

        public Builder persist(boolean persist) {
            this.persist = persist;
            return this;
        }

        public Builder peersList(Set<HostAddress> peersList) {
            this.peersList = peersList;
            return this;
        }

        public Builder workers(int workers) {
            this.workers = workers;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public PeerDiscoveryConfig build() {
            return new PeerDiscoveryConfig(includeHomeNode, ipAddress, externalIpAddress, touchMaxNodes, touchPeriod, persist, peersList, workers, enabled);
        }
    }

}
