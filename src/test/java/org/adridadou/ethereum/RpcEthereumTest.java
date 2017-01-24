package org.adridadou.ethereum;


import org.adridadou.ethereum.blockchain.Web3JFacade;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.provider.EthereumFacadeRpcProvider;
import org.adridadou.ethereum.values.*;
import org.ethereum.crypto.ECKey;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by davidroon on 19.11.16.
 * This code is released under Apache 2 license
 */
public class RpcEthereumTest {

    private final EthereumFacadeRpcProvider provider = new EthereumFacadeRpcProvider();
    private final Web3JFacade web3j = mock(Web3JFacade.class);
    private final SoliditySource contractSource = new SoliditySource(
            "pragma solidity ^0.4.6;" +
                    "contract myContract2 {" +
                    "  int i1;" +
                    "  function myMethod(int value) {i1 = value;}" +
                    "  function getI1() constant returns (int) {return i1;}" +
                    "}");

    private final EthAccount account = new EthAccount(ECKey.fromPrivate(BigInteger.ONE));
    private final EthAddress address = EthAddress.of("0x3939393848");

    @Test
    public void test() throws IOException, ExecutionException, InterruptedException {
        when(web3j.getOutputTypeHandler()).thenReturn(new OutputTypeHandler());
        when(web3j.getTransactionCount(account.getAddress())).thenReturn(BigInteger.TEN);
        when(web3j.getGasPrice()).thenReturn(BigInteger.TEN);
        when(web3j.estimateGas(eq(account), any(EthData.class))).thenReturn(BigInteger.TEN);
        when(web3j.constantCall(eq(account), eq(address), any(EthData.class))).thenReturn(EthData.of(new byte[0]));

        EthereumFacade ethereum = provider.create(web3j, EthereumFacadeProvider.ROPSTEN_CHAIN_ID);
        CompiledContract compiledContract = ethereum.compile(contractSource, "myContract2").get();
        Contract service = ethereum.createContractProxy(compiledContract, address, account, Contract.class);

        service.myMethod(23).get();

        assertEquals(0, service.getI1().intValue());
    }

    private interface Contract {
        CompletableFuture<Void> myMethod(Integer value);

        Integer getI1();
    }
}
