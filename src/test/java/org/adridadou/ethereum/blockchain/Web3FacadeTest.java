package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.values.EthAddress;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by davidroon on 27.12.16.
 * This code is released under Apache 2 license
 */
public class Web3FacadeTest {
    private final Web3j web3j = mock(Web3j.class);
    private final Web3JFacade web3Facade = new Web3JFacade(web3j, new OutputTypeHandler());
    private final EthAddress address = EthAddress.of("0x00394857372832");

    @Test
    public void test_getBalance() throws IOException {
        Request req = mock(Request.class);
        EthGetBalance response = mock(EthGetBalance.class);
        when(req.send()).thenReturn(response);
        when(web3j.ethGetBalance(address.withLeading0x(), DefaultBlockParameterName.LATEST)).thenReturn(req);
        assertEquals(response, web3Facade.getBalance(address));
    }
}
