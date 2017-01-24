package org.adridadou.ethereum.keystore;

import com.google.common.collect.Lists;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.crypto.ECKey;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.util.encoders.Hex;
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
    public static EthAccount fromPrivateKey(final byte[] privateKey) {
        return new EthAccount(ECKey.fromPrivate(privateKey));
    }

    public static EthAccount fromPrivateKey(final String privateKey) {
        return new EthAccount(ECKey.fromPrivate(Hex.decode(privateKey)));
    }

    public static EthAccount from(final ECKey ecKey) {
        return new EthAccount(ecKey);
    }

    public static EthAccount from(final String id) {
        return new EthAccount(ECKey.fromPrivate(doSha3(id.getBytes(EthereumFacade.CHARSET))));
    }

    public static SecureKey from(final File file) {
        return new FileSecureKey(file);
    }

    public static List<SecureKey> listMainKeystores() {
        return listKeystores(new File(WalletUtils.getMainnetKeyDirectory()));
    }

    public static List<SecureKey> listRopstenKeystores() {
        return listKeystores(new File(WalletUtils.getTestnetKeyDirectory()));
    }

    public static List<SecureKey> listKeystores(final File directory) {
        File[] files = Optional.ofNullable(directory.listFiles()).orElseThrow(() -> new EthereumApiException("cannot find the folder " + WalletUtils.getMainnetKeyDirectory()));
        return Lists.newArrayList(files).stream()
                .filter(File::isFile)
                .map(AccountProvider::from)
                .collect(Collectors.toList());
    }

    private static byte[] doSha3(byte[] message) {
        SHA3Digest digest = new SHA3Digest(256);
        byte[] hash = new byte[digest.getDigestSize()];

        if (message.length != 0) {
            digest.update(message, 0, message.length);
        }
        digest.doFinal(hash, 0);
        return hash;
    }
}
