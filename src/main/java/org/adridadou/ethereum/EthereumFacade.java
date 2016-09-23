package org.adridadou.ethereum;

import com.google.common.base.Charsets;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.ethereum.crypto.ECKey;
import rx.Observable;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;

import static java.lang.reflect.Proxy.newProxyInstance;

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
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, code, contractName, address, sender);
        return proxy;
    }

    public <T> T createContractProxy(ContractAbi abi, EthAddress address, ECKey sender, Class<T> contractInterface) throws IOException {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, abi, address, sender);
        return proxy;
    }

    public Observable<EthAddress> publishContract(SoliditySource code, String contractName, ECKey sender) {
        return blockchainProxy.publish(code, contractName, sender);
    }

    public EthereumEventHandler events() {
        return blockchainProxy.events();
    }
}
