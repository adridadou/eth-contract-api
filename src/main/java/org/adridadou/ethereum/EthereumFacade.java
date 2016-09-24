package org.adridadou.ethereum;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.ethereum.crypto.ECKey;
import rx.Observable;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacade {
    public final static Charset CHARSET = Charsets.UTF_8;
    private final EthereumContractInvocationHandler handler;
    private final BlockchainProxy blockchainProxy;

    public EthereumFacade(BlockchainProxy blockchainProxy) {
        this.handler = new EthereumContractInvocationHandler(blockchainProxy);
        this.blockchainProxy = blockchainProxy;
    }

    public <T> T createContractProxy(SoliditySource code, String contractName, EthAddress address, ECKey sender, Class<T> contractInterface) throws IOException {
        handler.register(contractInterface, code, contractName, address, sender);
        return (T) Proxy.newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
    }

    public <T> T createContractProxy(ContractAbi abi, EthAddress address, ECKey sender, Class<T> contractInterface) throws IOException {
        handler.register(contractInterface, abi, address, sender);
        return (T) Proxy.newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
    }

    public Observable<EthAddress> publishContract(SoliditySource code, String contractName, ECKey sender, Object... constructorArgs) {
        return blockchainProxy.publish(code, contractName, sender, constructorArgs);
    }

    public EthereumEventHandler events() {
        return blockchainProxy.events();
    }
}
