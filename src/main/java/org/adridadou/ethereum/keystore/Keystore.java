package org.adridadou.ethereum.keystore;

import org.adridadou.exception.EthereumApiException;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.codehaus.jackson.map.ObjectMapper;
import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class Keystore {
    private KeystoreCrypto crypto;
    private String id;
    private Integer version;

    public Keystore() {
        this(null, null, null);
    }

    public Keystore(KeystoreCrypto crypto, String id, Integer version) {
        this.crypto = crypto;
        this.id = id;
        this.version = version;
    }

    public static ECKey fromKeystore(final File keystore, final String password) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Keystore ksObj = mapper.readValue(keystore, Keystore.class);
        byte[] cipherKey = checkMac(ksObj, password);
        byte[] secret = decryptAes(Hex.decode(ksObj.getCrypto().getCipherparams().getIv()), cipherKey, Hex.decode(ksObj.getCrypto().getCiphertext()));

        return ECKey.fromPrivate(secret);
    }

    private static byte[] decryptAes(byte[] iv, byte[] keyBytes, byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //Initialisation
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        //Mode
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(cipherText);
    }

    private static byte[] checkMac(Keystore keystore, String password) throws Exception {
        byte[] salt = Hex.decode(keystore.getCrypto().getKdfparams().getSalt());
        int iterations = keystore.getCrypto().getKdfparams().getC();
        byte[] part = new byte[16];
        byte[] h = hash(password, salt, iterations);
        byte[] cipherText = Hex.decode(keystore.getCrypto().getCiphertext());
        System.arraycopy(h, 16, part, 0, 16);

        byte[] actual = sha3(concat(part, cipherText));

        if (Arrays.equals(actual, Hex.decode(keystore.getCrypto().getMac()))) {
            System.arraycopy(h, 0, part, 0, 16);
            return part;
        }

        throw new EthereumApiException("error while loading the private key from the keystore. Most probably a wrong passphrase");
    }

    private static void p(byte[] c) {
        System.out.println(Hex.toHexString(c));
    }

    private static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private static byte[] hash(String encryptedData, byte[] salt, int iterations) throws Exception {
        char[] chars = encryptedData.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    private static byte[] sha3(byte[] h) throws NoSuchAlgorithmException {
        MessageDigest KECCAK = new Keccak.Digest256();
        KECCAK.reset();
        KECCAK.update(h);
        return KECCAK.digest();
    }

    public KeystoreCrypto getCrypto() {
        return crypto;
    }

    public void setCrypto(KeystoreCrypto crypto) {
        this.crypto = crypto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }


}
