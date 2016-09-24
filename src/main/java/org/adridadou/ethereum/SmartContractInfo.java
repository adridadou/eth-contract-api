package org.adridadou.ethereum;

import org.ethereum.crypto.ECKey;

/**
 * Created by davidroon on 21.09.16.
 * This code is released under Apache 2 license
 */
public class SmartContractInfo {
    private final EthAddress address;
    private final ECKey sender;

    public SmartContractInfo(EthAddress address, ECKey sender) {
        this.address = address;
        this.sender = sender;
    }

    public EthAddress getAddress() {
        return address;
    }

    public ECKey getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartContractInfo that = (SmartContractInfo) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        return sender != null ? sender.equals(that.sender) : that.sender == null;

    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        return result;
    }
}
