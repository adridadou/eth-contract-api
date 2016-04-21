package org.adridadou.util;

import org.adridadou.ethereum.keystore.Keystore;
import org.ethereum.crypto.ECKey;
import org.junit.Test;

import java.io.File;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class KeystoreTest {

    @Test
    public void test() throws Exception {
        final ECKey ecKey = Keystore.fromKeystore(new File("src/test/resources/keystore.json"), "testpassword");
        assertEquals(new BigInteger("55254095649631781209224057814590225966912998986153936485890744796566334537373"), ecKey.getPrivKey());
    }
}
