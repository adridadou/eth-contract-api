package org.adridadou.ethereum.values;


import org.ethereum.crypto.ECKey;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

/**
 * Created by davidroon on 05.11.16.
 * This code is released under Apache 2 license
 */
public class EthAccount {
    public final ECKey key;
    public final Credentials credentials;

    public EthAccount(ECKey key) {
        this.key = key;
        ECKeyPair keyPair = ECKeyPair.create(key.getPrivKey());
        this.credentials = Credentials.create(keyPair);
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


    @Override
    public String toString() {
        return "account address:" + getAddress().withLeading0x();
    }
}
