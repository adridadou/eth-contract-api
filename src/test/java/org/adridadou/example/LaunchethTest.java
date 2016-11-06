package org.adridadou.example;

import org.adridadou.ethereum.EthAccount;
import org.adridadou.ethereum.EthAddress;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.SoliditySource;
import org.adridadou.ethereum.provider.*;
import org.ethereum.crypto.ECKey;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
@Ignore
public class LaunchethTest {


    @Test
    public void run() throws Exception {
        EthereumFacadeProvider testnet = new TestnetEthereumFacadeProvider();
        EthAccount sender = testnet.getKey("cow").decode("");
        EthereumFacade ethereum = testnet.create();

        File contractSrc = new File(this.getClass().getResource("/launcheth.sol").toURI());
        SoliditySource contract = SoliditySource.from(contractSrc);

        CompletableFuture<EthAddress> address = ethereum.publishContract(contract, "Launcheth", sender);
        Launcheth myContract = ethereum
                .createContractProxy(contract, "Launcheth", address.get(), sender, Launcheth.class);

        myContract.register("myNamespace", "myName", "1.0", "http", "http://mywebsite.com/myfile", "mychecksum");
    }

    private interface Launcheth {
        /*
          Register a new package.

          params:
          - Namespace: The namespace of the package you are looking for
          - name: the name of the legal documents
          - version: the version of the legal documents
        */
        void register(String namespace, String name, String version, String protocol, String source, String checksum);

        /*
          Create a new namespace and makes the tx.origin the owner of this namespace
        */
        void createNamespace(String namespace, String owner);

        /*
          Passes the ownership of a namespace to someone else
        */
        void changeOwner(String namespace, String newOwner);

        /*
            Get the source of the package.

            params:
            - Namespace: The namespace of the package you are looking for
            - name: the name of the legal documents
            - version: the version of the legal documents
            - protocol: which protocol you want to get (http, ipfs, etc ... )
        */
        String getSource(String namespace, String name, String version, String protocol);

        String getChecksum(String namespace, String name, String version);

        Boolean canWrite(String namespace, String user);

        Integer getNbNamespaces();

        String getNamespace(Integer id);

        Integer getNbProjects(String namespace);

        String getProject(String namespace, Integer id);

        Integer getNbVersions(String namespace, String project);

        String getVersion(String namespace, String project, Integer id);

        EthAddress getOwner(String namespace);
    }
}
