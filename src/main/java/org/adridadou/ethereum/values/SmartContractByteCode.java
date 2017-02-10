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
        int length = length1 + length2 * 256;
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
        byte[] hash = new byte[32];
        System.arraycopy(link, 7, hash, 0, 32);
        return new SwarmMetadaLink(SwarmHash.of(hash));
    }

    public String toString() {
        return Hex.toHexString(code);
    }
}
