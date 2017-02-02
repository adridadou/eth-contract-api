package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidroon on 22.01.17.
 */
public class TestConfig {

    private final Date initialTime;
    private final long gasLimit;
    private final long gasPrice;
    private final Map<EthAccount, EthValue> balances;

    public TestConfig(Date initialTime, long gasLimit, long gasPrice, Map<EthAccount, EthValue> balances) {
        this.initialTime = initialTime;
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
        this.balances = balances;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public Map<EthAccount, EthValue> getBalances() {
        return balances;
    }

    public Date getInitialTime() {
        return initialTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long gasLimit = 4_700_000;
        private long gasPrice = 50_000_000_000L;
        private final Map<EthAccount, EthValue> balances = new HashMap<>();
        private Date initialTime = new Date();

        public Builder gasLimit(long gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder gasPrice(long gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public Builder balance(EthAccount account, EthValue value) {
            this.balances.put(account,value);
            return this;
        }

        public Builder initialTime(Date date) {
            this.initialTime = date;
            return this;
        }

        public TestConfig build() {
            return new TestConfig(initialTime, gasLimit, gasPrice, balances);
        }
    }
}
