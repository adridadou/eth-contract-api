package org.adridadou.ethereum.keystore;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.crypto.ECKey;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 26.12.16.
 * This code is released under Apache 2 license
 */
public class AccountProvider {
    public EthAccount fromString(final String id) {
        return new EthAccount(ECKey.fromPrivate(doSha3(id.getBytes(EthereumFacade.CHARSET))));
    }

    public SecureKey fromFile(final File file) {
        return new FileSecureKey(file);
    }

    public List<SecureKey> listMainKeystores() {
        return listKeystores(new File(WalletUtils.getMainnetKeyDirectory()));
    }

    public List<SecureKey> listRopstenKeystores() {
        return listKeystores(new File(WalletUtils.getTestnetKeyDirectory()));
    }

    public List<SecureKey> listKeystores(final File directory) {
        File[] files = Optional.ofNullable(directory.listFiles()).orElseThrow(() -> new EthereumApiException("cannot find the folder " + WalletUtils.getMainnetKeyDirectory()));
        return Lists.newArrayList(files).stream()
                .filter(File::isFile)
                .map(FileSecureKey::new)
                .collect(Collectors.toList());
    }

    private byte[] doSha3(byte[] message) {
        SHA3Digest digest = new SHA3Digest(256);
        byte[] hash = new byte[digest.getDigestSize()];

        if (message.length != 0) {
            digest.update(message, 0, message.length);
        }
        digest.doFinal(hash, 0);
        return hash;
    }
}
