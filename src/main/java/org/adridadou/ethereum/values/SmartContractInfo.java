package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 21.09.16.
 * This code is released under Apache 2 license
 */
public class SmartContractInfo {
    private final EthAddress address;
    private final EthAccount sender;

    public SmartContractInfo(EthAddress address, EthAccount sender) {
        this.address = address;
        this.sender = sender;
    }

    public EthAddress getAddress() {
        return address;
    }

    public EthAccount getSender() {
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
