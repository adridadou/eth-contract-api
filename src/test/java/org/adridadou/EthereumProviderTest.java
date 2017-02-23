package org.adridadou;

import org.adridadou.ethereum.*;
import org.adridadou.ethereum.ethj.EthereumTest;
import org.adridadou.ethereum.ethj.TestConfig;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.*;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumProviderTest {
    private final EthereumTest ethereumj = new EthereumTest(TestConfig.builder().build());
    private final InputTypeHandler inputTypeHandler = new InputTypeHandler();
    private final OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
    private final EthereumEventHandler handler = new EthereumEventHandler();
    private final EthereumProxy bcProxy = new EthereumProxy(ethereumj, handler, inputTypeHandler, outputTypeHandler);
    private final EthAccount account = ethereumj.defaultAccount();
    private final EthereumFacade ethereum = new EthereumFacade(bcProxy, inputTypeHandler, outputTypeHandler, SwarmService.from(SwarmService.PUBLIC_HOST), SolidityCompiler.getInstance());

    @Before
    public void before() {
        handler.onReady();
    }

    @Test
    public void checkSuccessCase() throws IOException, ExecutionException, InterruptedException {
        SoliditySource contractSource = SoliditySourceString.from(
                "pragma solidity ^0.4.6;" +
                        "contract myContract {" +
                        "  int i1;" +
                        "  function myMethod() constant returns (int) {" +
                        "    return 23;" +
                        "  }" +
                        "}");
        CompiledContract compiledContract = ethereum.compile(contractSource).get().get("myContract");
        EthAddress address = ethereum.publishContract(compiledContract, account).get();

        MyContract proxy = ethereum.createContractProxy(compiledContract, address, account, MyContract.class);

        assertEquals(23, proxy.myMethod());
    }

    @Test
    public void checkCreateTx() throws IOException, ExecutionException, InterruptedException {
        SoliditySource contractSource = SoliditySourceString.from(
                "pragma solidity ^0.4.6;" +
                        "contract myContract2 {" +
                        "  int i1;" +
                        "  function myMethod(int value) {i1 = value;}" +
                        "  function getI1() constant returns (int) {return i1;}" +
                        "}");

        CompiledContract compiledContract = ethereum.compile(contractSource).get().get("myContract2");

        EthAddress address = ethereum.publishContract(compiledContract, account).get();

        BlaBla proxy = ethereum.createContractProxy(compiledContract, address, account, BlaBla.class);
        proxy.myMethod(12).get();

        assertEquals(12, proxy.getI1());
    }

    private interface MyContract {
        int myMethod();
    }

    private interface BlaBla {
        CompletableFuture<Void> myMethod(int value);

        int getI1();
    }
}
