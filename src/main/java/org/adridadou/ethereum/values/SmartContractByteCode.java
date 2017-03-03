package org.adridadou.ethereum.values;

import com.google.common.base.Charsets;
import org.adridadou.ethereum.swarm.SwarmHash;
import org.adridadou.exception.EthereumApiException;
import org.spongycastle.util.encoders.Hex;

import java.util.Optional;

/**
 * Created by davidroon on 17.12.16.
 * This code is released under Apache 2 license
 */
public class SmartContractByteCode {
    public static final int WORD_SIZE = 256;
    public static final int HASH_SIZE = 32;
    public static final int START_OF_LINK_INDEX = 7;
    private final byte[] code;

    public SmartContractByteCode(byte[] code) {
        this.code = code;
    }

    public static SmartContractByteCode of(EthData code) {
        return new SmartContractByteCode(code.data);
    }

    public static SmartContractByteCode of(byte[] code) {
        return new SmartContractByteCode(code);
    }

    public static SmartContractByteCode of(String code) {
        return new SmartContractByteCode(Hex.decode(code));
    }

    public Optional<SwarmMetadaLink> getMetadaLink() {
        if (code.length == 0) {
            return Optional.empty();
        }

        byte length1 = code[code.length - 1];
        byte length2 = code[code.length - 2];
        int length = length1 + length2 * WORD_SIZE;
        if (length < code.length) {
            byte[] link = new byte[length];
            System.arraycopy(code, code.length - length, link, 0, length);
            return Optional.of(toMetaDataLink(link));
        }
        return Optional.empty();
    }

    private SwarmMetadaLink toMetaDataLink(byte[] link) {
        String strLink = new String(link, Charsets.UTF_8);
        if (strLink.startsWith("bzzr0")) {
            return toSwarmMetadataLink(link);
        }
        throw new EthereumApiException("unknown protocol forNetwork " + strLink);
    }

    private SwarmMetadaLink toSwarmMetadataLink(byte[] link) {
        byte[] hash = new byte[HASH_SIZE];
        System.arraycopy(link, START_OF_LINK_INDEX, hash, 0, HASH_SIZE);
        return new SwarmMetadaLink(SwarmHash.of(hash));
    }

    public String toString() {
        return Hex.toHexString(code);
    }

    public boolean isEmpty() {
        return code.length == 0;
    }
}
