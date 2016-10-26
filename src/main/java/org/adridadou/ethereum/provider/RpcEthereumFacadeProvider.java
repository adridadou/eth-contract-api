package org.adridadou.ethereum.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.adridadou.ethereum.BlockchainProxyRpc;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.adridadou.exception.EthereumApiException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class RpcEthereumFacadeProvider implements EthereumFacadeProvider {

    public EthereumFacade create(final String url) {

        Web3j web3j = Web3j.build(new HttpService(url));
        return new EthereumFacade(new BlockchainProxyRpc(web3j));
    }

    @Override
    public EthereumFacade create() {
        return create("http://localhost:8545/");
    }

    @Override
    public SecureKey getKey(String id) throws Exception {
        File[] files = new File(getKeystoreFolderPath()).listFiles();

        return Lists.newArrayList(Preconditions.checkNotNull(files, "the folder " + getKeystoreFolderPath() + " cannot be found"))
                .stream().filter(file -> id.equals(file.getName()))
                .findFirst().map(FileSecureKey::new)
                .orElseThrow(() -> new EthereumApiException("the file " + id + " could not be found"));
    }

    @Override
    public List<? extends SecureKey> listAvailableKeys() {
        File[] files = Optional.ofNullable(new File(getKeystoreFolderPath()).listFiles()).orElseThrow(() -> new EthereumApiException("cannot find the folder " + getKeystoreFolderPath()));
        return Lists.newArrayList(files).stream()
                .filter(File::isFile)
                .map(FileSecureKey::new)
                .collect(Collectors.toList());
    }

    private String getKeystoreFolderPath() {
        String homeDir = System.getProperty("user.home");
        return homeDir + "/Library/Ethereum/keystore/";
    }

    protected void setSslCertificate(final String fileName) throws KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        System.setProperty("javax.net.ssl.trustStore", fileName);
    }

    public static class ImportCA {
        public static void importCA(final String certfile, final InputStream is, final String pass, final String alias) throws Exception {

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = pass.toCharArray();
            keystore.load(is, password);
//////
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certstream = fullStream(certfile);
            Certificate certs = cf.generateCertificate(certstream);
            File keystoreFile = new File(certfile);
// Load the keystore contents
            FileInputStream in = new FileInputStream(keystoreFile);
            keystore.load(in, password);
            in.close();

// Add the certificate
            keystore.setCertificateEntry(alias, certs);

// Save the new keystore contents
            FileOutputStream out = new FileOutputStream(keystoreFile);
            keystore.store(out, password);
            out.close();
        }

        private static InputStream fullStream(String fname) throws IOException {
            FileInputStream fis = new FileInputStream(fname);
            DataInputStream dis = new DataInputStream(fis);
            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            return new ByteArrayInputStream(bytes);
        }
    }
}
