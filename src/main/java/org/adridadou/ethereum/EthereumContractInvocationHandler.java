package org.adridadou.ethereum;

import com.google.common.collect.Sets;
import org.adridadou.ethereum.blockchain.BlockchainProxy;
import org.adridadou.ethereum.converters.input.*;
import org.adridadou.ethereum.converters.output.*;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.ContractNotFoundException;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumContractInvocationHandler implements InvocationHandler {

    private final Map<EthAddress, Map<EthAccount, SmartContract>> contracts = new HashMap<>();
    private final BlockchainProxy blockchainProxy;
    private final InputTypeHandler inputTypeHandler;
    private final OutputTypeHandler outputTypeHandler;
    private final Map<ProxyWrapper, SmartContractInfo> info = new HashMap<>();


    EthereumContractInvocationHandler(BlockchainProxy blockchainProxy, InputTypeHandler inputTypeHandler, OutputTypeHandler outputTypeHandler) {
        this.blockchainProxy = blockchainProxy;
        this.inputTypeHandler = inputTypeHandler;
        this.outputTypeHandler = outputTypeHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        SmartContractInfo contractInfo = info.get(new ProxyWrapper(proxy));
        SmartContract contract = contracts.get(contractInfo.getAddress()).get(contractInfo.getSender());
        Object[] arguments = Optional.ofNullable(args).map(this::prepareArguments).orElse(new Object[0]);
        if (method.getReturnType().equals(Void.TYPE)) {
            try {
                contract.callFunction(methodName, arguments).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }

            return Void.TYPE;
        } else if (method.getReturnType().equals(CompletableFuture.class)) {
                return contract.callFunction(methodName, arguments).thenApply(result -> convertResult(result, method));
        } else {
            return convertResult(contract.callConstFunction(methodName, arguments), method);
        }
    }

    private Object[] prepareArguments(Object[] args) {

        return Arrays.stream(args)
                .map(inputTypeHandler::convert)
                .toArray();
    }

    private Object convertResult(Object[] result, Method method) {
        if (result.length == 0) {
            return convertResult(null, method.getReturnType(), method.getGenericReturnType());
        }
        if (result.length == 1) {
            return convertResult(result[0], method.getReturnType(), method.getGenericReturnType());
        }
        return convertSpecificType(result, method.getReturnType());
    }

    private Object convertSpecificType(Object[] result, Class<?> returnType) {
        Object[] params = new Object[result.length];

        Constructor constr = lookForNonEmptyConstructor(returnType, result);

        for (int i = 0; i < result.length; i++) {
            params[i] = convertResult(result[i], constr.getParameterTypes()[i], constr.getGenericParameterTypes()[i]);
        }

        try {
            return constr.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new EthereumApiException("error while converting to a specific type", e);
        }
    }

    private Object convertResult(Object result, Class<?> returnType, Type genericType) {
        return outputTypeHandler.getConverter(returnType)
                .map(converter -> converter.convert(result, returnType.isArray() ? returnType.getComponentType() : genericType))
                .orElseGet(() -> convertSpecificType(new Object[]{result}, returnType));
    }

    private Constructor lookForNonEmptyConstructor(Class<?> returnType, Object[] result) {
        for (Constructor constructor : returnType.getConstructors()) {
            if (constructor.getParameterCount() > 0) {
                if (constructor.getParameterCount() != result.length) {
                    throw new IllegalArgumentException("the number of arguments don't match for type " + returnType.getSimpleName() + ". Constructor has " + constructor.getParameterCount() + " and result has " + result.length);
                }
                return constructor;
            }
        }
        throw new IllegalArgumentException("no constructor with arguments found! for type " + returnType.getSimpleName());
    }

    <T> void register(T proxy, Class<T> contractInterface, SoliditySource code, String contractName, EthAddress address, EthAccount sender) throws IOException {
        final Map<String, CompilationResult.ContractMetadata> contractsFound = compile(code.getSource()).contracts;
        CompilationResult.ContractMetadata found = null;
        for (Map.Entry<String, CompilationResult.ContractMetadata> entry : contractsFound.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(contractName)) {
                if (found != null) {
                    throw new EthereumApiException("more than one Contract found for " + contractInterface.getSimpleName());
                }
                found = entry.getValue();
            }
        }
        if (found == null) {
            throw new ContractNotFoundException("no contract found for " + contractInterface.getSimpleName());
        }
        SmartContract smartContract = blockchainProxy.map(code, contractName, address, sender);

        verifyContract(smartContract, contractInterface);
        info.put(new ProxyWrapper(proxy), new SmartContractInfo(address, sender));
        Map<EthAccount, SmartContract> proxies = contracts.getOrDefault(address, new HashMap<>());
        proxies.put(sender, smartContract);
        contracts.put(address, proxies);
    }

    <T> void register(T proxy, Class<T> contractInterface, ContractAbi abi, EthAddress address, EthAccount sender) {
        SmartContract smartContract = blockchainProxy.mapFromAbi(abi, address, sender);
        verifyContract(smartContract, contractInterface);

        info.put(new ProxyWrapper(proxy), new SmartContractInfo(address, sender));
        Map<EthAccount, SmartContract> proxies = contracts.getOrDefault(address, new HashMap<>());
        proxies.put(sender, smartContract);
        contracts.put(address, proxies);
    }

    private void verifyContract(SmartContract smartContract, Class<?> contractInterface) {
        Set<Method> interfaceMethods = Sets.newHashSet(contractInterface.getMethods());
        Set<CallTransaction.Function> solidityMethods = smartContract.getFunctions().stream().filter(f -> f != null).collect(Collectors.toSet());

        Set<String> interfaceMethodNames = interfaceMethods.stream().map(Method::getName).collect(Collectors.toSet());
        Set<String> solidityFuncNames = solidityMethods.stream().map(d -> d.name).collect(Collectors.toSet());

        Sets.SetView<String> superfluous = Sets.difference(interfaceMethodNames, solidityFuncNames);

        if (!superfluous.isEmpty()) {
            throw new EthereumApiException("superflous function definition in interface " + contractInterface.getName() + ":" + superfluous.toString());
        }

        Map<String, Method> methods = interfaceMethods.stream().collect(Collectors.toMap(Method::getName, Function.identity()));

        for (CallTransaction.Function func : solidityMethods) {
            if (methods.get(func.name) != null && func.inputs.length != methods.get(func.name).getParameterCount()) {
                throw new EthereumApiException("parameter count mismatch for " + func.name + " on contract " + contractInterface.getName());
            }
        }
    }

    private CompilationResult compile(final String contract) throws IOException {
        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(EthereumFacade.CHARSET), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.INTERFACE);
        return CompilationResult.parse(res.output);
    }
}
