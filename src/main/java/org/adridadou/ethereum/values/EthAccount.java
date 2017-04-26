package org.adridadou.ethereum.values;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;

/**
 * Created by davidroon on 05.11.16.
 * This code is released under Apache 2 license
 */
public class EthAccount {
    private final BigInteger privateKey;

    public EthAccount(BigInteger privateKey) {
        this.privateKey = privateKey;
    }

    public EthAddress getAddress() {
        Credentials credentials = Credentials.create(ECKeyPair.create(privateKey));
        return EthAddress.of(credentials.getAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthAccount that = (EthAccount) o;

        return privateKey != null ? privateKey.equals(that.privateKey) : that.privateKey == null;
    }

    @Override
    public int hashCode() {
        return privateKey != null ? privateKey.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "account address:" + getAddress().withLeading0x();
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }
}
