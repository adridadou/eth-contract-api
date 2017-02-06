package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 21.09.16.
 * This code is released under Apache 2 license
 */
public class SmartContractInfo {
    private final EthAddress address;
    private final EthAccount account;

    public SmartContractInfo(EthAddress address, EthAccount account) {
        this.address = address;
        this.account = account;
    }

    public EthAddress getAddress() {
        return address;
    }

    public EthAccount getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartContractInfo that = (SmartContractInfo) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        return account != null ? account.equals(that.account) : that.account == null;

    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SmartContractInfo{" +
                "address=" + address.withLeading0x() +
                ", account=" + account.getAddress().withLeading0x() +
                '}';
    }
}
