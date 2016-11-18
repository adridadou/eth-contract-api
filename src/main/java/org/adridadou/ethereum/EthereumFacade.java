package org.adridadou.ethereum;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Charsets;
import org.adridadou.ethereum.blockchain.BlockchainProxy;
import org.adridadou.ethereum.converters.input.InputTypeConverter;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeConverter;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.values.*;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacade {
    public final static Charset CHARSET = Charsets.UTF_8;
    private final EthereumContractInvocationHandler handler;
    private final OutputTypeHandler outputTypeHandler;
    private final InputTypeHandler inputTypeHandler;
    private final BlockchainProxy blockchainProxy;

    public EthereumFacade(BlockchainProxy blockchainProxy) {
        inputTypeHandler = new InputTypeHandler();
        outputTypeHandler = new OutputTypeHandler();
        this.handler = new EthereumContractInvocationHandler(blockchainProxy, inputTypeHandler, outputTypeHandler);
        this.blockchainProxy = blockchainProxy;
    }

    public EthereumFacade addInputHandlers(final List<InputTypeConverter<?>> handlers) {
        inputTypeHandler.addConverters(handlers);
        return this;
    }

    public EthereumFacade addOutputHandlers(final List<OutputTypeConverter> handlers) {
        outputTypeHandler.addConverters(handlers);
        return this;
    }

    public <T> T createContractProxy(SoliditySource code, String contractName, EthAddress address, EthAccount sender, Class<T> contractInterface) throws IOException {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, code, contractName, address, sender);
        return proxy;
    }

    public <T> T createContractProxy(ContractAbi abi, EthAddress address, EthAccount sender, Class<T> contractInterface) throws IOException {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, abi, address, sender);
        return proxy;
    }

    public CompletableFuture<EthAddress> publishContract(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs) {
        return blockchainProxy.publish(code, contractName, sender, constructorArgs);
    }

    public boolean addressExists(final EthAddress address) {
        return blockchainProxy.addressExists(address);
    }

    public EthValue getBalance(final EthAddress addr) {
        return blockchainProxy.getBalance(addr);
    }

    public EthValue getBalance(final EthAccount account) {
        return blockchainProxy.getBalance(account.getAddress());
    }

    public EthereumEventHandler events() {
        return blockchainProxy.events();
    }

}
