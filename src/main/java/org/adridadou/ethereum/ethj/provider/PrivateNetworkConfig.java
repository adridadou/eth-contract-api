package org.adridadou.ethereum.ethj.provider;

import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidroon on 20.11.16.
 * This code is released under Apache 2 license
 */
public class PrivateNetworkConfig {
    private final Map<EthAccount, EthValue> initialBalances = new HashMap<>();
    private boolean resetPrivateBlockchain;
    private String dbName = "privateIntegration";


    public PrivateNetworkConfig initialBalance(final EthAccount account, final EthValue value) {
        initialBalances.put(account, value);
        return this;
    }

    public Map<EthAccount, EthValue> getInitialBalances() {
        return initialBalances;
    }

    public boolean isResetPrivateBlockchain() {
        return resetPrivateBlockchain;
    }

    public String getDbName() {
        return dbName;
    }

    public PrivateNetworkConfig dbName(final String name) {
        this.dbName = name;
        return this;
    }

    public PrivateNetworkConfig reset(final boolean reset) {
        this.resetPrivateBlockchain = reset;
        return this;
    }

    public static PrivateNetworkConfig config() {
        return new PrivateNetworkConfig();
    }
}
