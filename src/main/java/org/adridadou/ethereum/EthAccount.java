package org.adridadou.ethereum;


import org.ethereum.crypto.ECKey;

/**
 * Created by davidroon on 05.11.16.
 * This code is released under Apache 2 license
 */
public class EthAccount {
    public final ECKey key;

    public EthAccount(ECKey key) {
        this.key = key;
    }

    public EthAddress getAddress() {
        return EthAddress.of(key.getAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthAccount that = (EthAccount) o;

        return key != null ? key.equals(that.key) : that.key == null;

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
