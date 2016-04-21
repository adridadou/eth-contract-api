package org.adridadou.ethereum.keystore;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;
import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
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
        System.out.println(aesDecrypt(ksObj, password));
        return null;
    }

    private static String aesDecrypt(Keystore keystore, String password) throws Exception {
        byte[] salt = Hex.decode(keystore.getCrypto().getKdfparams().getSalt());
        int iterations = keystore.getCrypto().getKdfparams().getC();
        byte[] part = new byte[16];
        byte[] h = hash(password, salt, iterations);
        byte[] cipherText = Hex.decode(keystore.getCrypto().getCiphertext());
        System.arraycopy(h, 15, part, 0, 16);


        return Hex.toHexString(concat(part, cipherText));
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
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(spec).getEncoded();
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
