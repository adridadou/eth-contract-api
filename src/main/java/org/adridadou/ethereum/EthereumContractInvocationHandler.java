package org.adridadou.ethereum;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.adridadou.ethereum.converters.*;
import org.adridadou.exception.ContractNotFoundException;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.blockchain.SolidityContract;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumContractInvocationHandler implements InvocationHandler {

    private final Map<String, SolidityContract> contracts = Maps.newHashMap();
    private final BlockchainProxy blockchainProxy;
    private final List<TypeHandler<?>> handlers;

    public EthereumContractInvocationHandler(BlockchainProxy blockchainProxy) {
        this.blockchainProxy = blockchainProxy;
        handlers = Lists.newArrayList(
                new IntegerHandler(),
                new LongHandler(),
                new StringHandler(),
                new BooleanHandler(),
                new AddressHandler()
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String contractName = method.getDeclaringClass().getSimpleName().toLowerCase();
        final String methodName = method.getName();
        SolidityContract contract = contracts.get(contractName);
        Object[] arguments = args == null ? new Object[0] : args;
        if (method.getReturnType().equals(Void.TYPE)) {
            contract.callFunction(methodName, arguments);
            return null;
        } else {
            Object[] result = contract.callConstFunction(methodName, arguments);
            if (result.length == 1) {
                return convertResult(result[0], method.getReturnType(), method.getGenericReturnType());
            }

            return convertSpecificType(result, method.getReturnType());
        }
    }

    private Object convertSpecificType(Object[] result, Class<?> returnType) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object[] params = new Object[result.length];

        Constructor constr = lookForNonEmptyConstructor(returnType, result);

        for (int i = 0; i < result.length; i++) {
            params[i] = convertResult(result[i], constr.getParameterTypes()[i], constr.getGenericParameterTypes()[i]);
        }


        return constr.newInstance(params);
    }

    private Class<?> getCollectionType(Class<?> returnType, Type genericType) {
        if (returnType.isArray()) {
            return returnType.getComponentType();
        }
        if (List.class.equals(returnType)) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
        return null;
    }

    private <T> T[] convertArray(Class<T> cls, Object[] arr) {
        for (TypeHandler<?> handler : handlers) {
            if (handler.isOfType(cls)) {
                T[] result = (T[]) Array.newInstance(cls, arr.length);
                for (int i = 0; i < arr.length; i++) {
                    result[i] = (T) handler.convert(arr[i]);
                }
                return result;
            }
        }
        throw new IllegalArgumentException("no handler founds to convert " + cls.getSimpleName());
    }

    private <T> List<T> convertList(Class<T> cls, Object[] arr) {
        for (TypeHandler<?> handler : handlers) {
            if (handler.isOfType(cls)) {
                List<T> result = new ArrayList<>();
                for (Object obj : arr) {
                    result.add((T) handler.convert(obj));
                }
                return result;
            }
        }
        throw new IllegalArgumentException("no handler founds to convert " + cls.getSimpleName());
    }

    private Object convertResult(Object result, Class<?> returnType, Type genericType) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> arrType = getCollectionType(returnType, genericType);
        if (arrType != null) {
            if (returnType.isArray()) {
                return convertArray(arrType, (Object[]) result);
            }

            return convertList(arrType, (Object[]) result);
        }

        for (TypeHandler<?> handler : handlers) {
            if (handler.isOfType(returnType)) {
                return handler.convert(result);
            }
        }

        return convertSpecificType(new Object[]{result}, returnType);
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

        contracts.put(contractInterface.getSimpleName().toLowerCase(), blockchainProxy.map(code, address));
    }

    private CompilationResult compile(final String contract) throws IOException {
        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.INTERFACE);

        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");

        return CompilationResult.parse(res.output);
    }
}
