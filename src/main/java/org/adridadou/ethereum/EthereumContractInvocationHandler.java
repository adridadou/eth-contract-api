package org.adridadou.ethereum;

import com.google.common.collect.Maps;
import org.adridadou.exception.ContractNotFoundException;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.blockchain.SolidityContract;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumContractInvocationHandler implements InvocationHandler {

    private final Map<String, SolidityContract> contracts = Maps.newHashMap();
    private final BlockchainProxy blockchainProxy;

    @Inject
    public EthereumContractInvocationHandler(BlockchainProxy blockchainProxy) {
        this.blockchainProxy = blockchainProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String contractName = proxy.getClass().getSimpleName().toLowerCase();
        final String methodName = method.getName();
        SolidityContract contract = contracts.get(contractName);
        return blockchainProxy.call(contract, methodName, args);
    }

    void register(Class<?> contractInterface, String code, EthAddress address) throws IOException {
        if (contracts.containsKey(contractInterface.getSimpleName())) {
            throw new EthereumApiException("attempt to register " + contractInterface.getSimpleName() + " twice!");
        }
        final Map<String, CompilationResult.ContractMetadata> contractsFound = compile(code).contracts;
        CompilationResult.ContractMetadata found = null;
        for (Map.Entry<String, CompilationResult.ContractMetadata> entry : contractsFound.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(contractInterface.getSimpleName())) {
                if (found != null) {
                    throw new EthereumApiException("more than one Contract found for " + contractInterface.getSimpleName());
                }
                found = entry.getValue();
            }
        }
        if (found == null) {
            throw new ContractNotFoundException("no contract found for " + contractInterface.getSimpleName());
        }

        contracts.put(contractInterface.getSimpleName().toLowerCase(), blockchainProxy.map(found.abi, address.address));
    }

    private CompilationResult compile(final String contract) throws IOException {
        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.INTERFACE);

        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");

        return CompilationResult.parse(res.output);
    }
}
