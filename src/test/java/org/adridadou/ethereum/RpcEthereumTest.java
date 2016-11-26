package org.adridadou.ethereum;


import org.adridadou.ethereum.blockchain.Web3JFacade;
import org.adridadou.ethereum.provider.RpcEthereumFacadeProvider;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.SoliditySource;
import org.ethereum.crypto.ECKey;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by davidroon on 19.11.16.
 * This code is released under Apache 2 license
 */
public class RpcEthereumTest {

    private final RpcEthereumFacadeProvider provider = new RpcEthereumFacadeProvider();
    private final Web3JFacade web3j = mock(Web3JFacade.class);
    private final SoliditySource contract = new SoliditySource(
            "contract myContract2 {" +
                    "  int i1;" +
                    "  function myMethod(int value) {i1 = value;}" +
                    "  function getI1() constant returns (int) {return i1;}" +
                    "}");

    private final EthAccount account = new EthAccount(ECKey.fromPrivate(BigInteger.ONE));
    private final EthAddress address = EthAddress.of("0x3939393848");

    @Test
    public void test() throws IOException, ExecutionException, InterruptedException {
        EthereumFacade ethereum = provider.create(web3j, (byte) 2);

        when(web3j.getTransactionCount(account)).thenReturn(BigInteger.TEN);
        when(web3j.getGasPrice()).thenReturn(BigInteger.TEN);
        when(web3j.estimateGas(eq(account), any(EthData.class))).thenReturn(BigInteger.TEN);
        when(web3j.constantCall(eq(account), eq(address), any(EthData.class))).thenReturn(EthData.of(new byte[0]));
        Contract service = ethereum.createContractProxy(contract, "myContract2", address, account, Contract.class);

        service.myMethod(23).get();

        assertEquals(0, service.getI1().intValue());
    }

    private interface Contract {
        CompletableFuture<Void> myMethod(Integer value);

        Integer getI1();
    }
}
