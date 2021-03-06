package org.adridadou.ethereum;

import com.google.common.collect.Sets;
import org.adridadou.ethereum.converters.future.*;
import org.adridadou.ethereum.converters.future.CompletableFutureConverter;
import org.adridadou.ethereum.converters.input.*;
import org.adridadou.ethereum.converters.output.*;
import org.adridadou.ethereum.values.*;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.values.EthValue.wei;


/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumContractInvocationHandler implements InvocationHandler {

    private final Map<EthAddress, Map<EthAccount, SmartContract>> contracts = new HashMap<>();
    private final EthereumProxy ethereumProxy;
    private final InputTypeHandler inputTypeHandler;
    private final OutputTypeHandler outputTypeHandler;
    private final Map<ProxyWrapper, SmartContractInfo> info = new HashMap<>();
    private final List<FutureConverter> futureConverters = new ArrayList<>();


    EthereumContractInvocationHandler(EthereumProxy ethereumProxy, InputTypeHandler inputTypeHandler, OutputTypeHandler outputTypeHandler) {
        this.ethereumProxy = ethereumProxy;
        this.inputTypeHandler = inputTypeHandler;
        this.outputTypeHandler = outputTypeHandler;
        this.futureConverters.add(new CompletableFutureConverter());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        SmartContractInfo contractInfo = info.get(new ProxyWrapper(proxy));
        SmartContract contract = contracts.get(contractInfo.getAddress()).get(contractInfo.getAccount());
        Object[] arguments = Optional.ofNullable(args).map(this::prepareArguments).orElse(new Object[0]);

        if (method.getReturnType().equals(Void.TYPE)) {
            try {
                contract.callFunction(methodName, arguments).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }

            return Void.TYPE;
        } else {
            return findConverter(method.getReturnType()).map(converter -> {
                if (converter.isFutureType(method.getReturnType())) {
                    return converter.convert(contract.callFunction(methodName, arguments).thenApply(result -> convertResult(result, method)));
                }
                return converter.getPayable(contract, methodName, arguments, method, this);
            }).orElseGet(() -> convertResult(contract.callConstFunction(methodName, wei(0), arguments), method));
        }
    }

    private Optional<FutureConverter> findConverter(Class type) {
        return futureConverters.stream().filter(converter -> converter.isFutureType(type) || converter.isPayableType(type)).findFirst();
    }

    private Object[] prepareArguments(Object[] args) {
        return Arrays.stream(args)
                .map(inputTypeHandler::convert)
                .toArray();
    }

    public Object convertResult(Object[] result, Method method) {
        if (result.length == 0) {
            return outputTypeHandler.convertResult(null, method.getReturnType(), method.getGenericReturnType());
        }
        if (result.length == 1) {
            return outputTypeHandler.convertResult(result[0], method.getReturnType(), method.getGenericReturnType());
        }
        return outputTypeHandler.convertSpecificType(result, method.getReturnType());
    }

    protected <T> void register(T proxy, Class<T> contractInterface, ContractAbi abi, EthAddress address, EthAccount account) {
        if(address.isEmpty()) {
            throw new EthereumApiException("the contract address cannot be empty");
        }
        SmartContract smartContract = ethereumProxy.mapFromAbi(abi, address, account);
        verifyContract(smartContract, contractInterface);

        info.put(new ProxyWrapper(proxy), new SmartContractInfo(address, account));
        Map<EthAccount, SmartContract> proxies = contracts.getOrDefault(address, new HashMap<>());
        proxies.put(account, smartContract);
        contracts.put(address, proxies);
    }

    private void verifyContract(SmartContract smartContract, Class<?> contractInterface) {
        Set<Method> interfaceMethods = Sets.newHashSet(contractInterface.getMethods());
        Set<CallTransaction.Function> solidityMethods = smartContract.getFunctions().stream().filter(Objects::nonNull).collect(Collectors.toSet());

        Set<String> interfaceMethodNames = interfaceMethods.stream().map(Method::getName).collect(Collectors.toSet());
        Set<String> solidityFuncNames = solidityMethods.stream().map(d -> d.name).collect(Collectors.toSet());

        Sets.SetView<String> superfluous = Sets.difference(interfaceMethodNames, solidityFuncNames);

        if (!superfluous.isEmpty()) {
            throw new EthereumApiException("The contract " + contractInterface.getName() + " does not have the function(s) " + superfluous.toString() + ". Add this function(s) to the smart contract or remove it fromSeed your interface");
        }

        Map<String, List<Method>> methods = interfaceMethods.stream().collect(Collectors.groupingBy(Method::getName));

        for (CallTransaction.Function func : solidityMethods) {

            if(func.name == null || func.name.isEmpty()){
                continue;
            }

            Optional.ofNullable(methods.get(func.name)).ifPresent(methodList -> {
                Method method = methodList.stream().filter(m -> m.getParameterCount() == func.inputs.length)
                        .findFirst()
                        .orElseThrow(() -> new EthereumApiException("No function " + func.name + " found with " + func.inputs.length + " parameters on contract " + contractInterface.getName()));
                boolean isPayableType = findConverter(method.getReturnType()).map(converter -> converter.isPayableType(method.getReturnType())).orElse(false);
                boolean isFutureType = findConverter(method.getReturnType()).map(converter -> converter.isFutureType(method.getReturnType())).orElse(false);
                if(func.payable != isPayableType) {
                    throw new EthereumApiException("ABI definition of " + func.name + " for payable is " + func.payable + " but return type is " + method.getReturnType().getSimpleName() + ". Return type should be Payable if and only if the function is payable");
                }

                if(func.constant && isFutureType) {
                    throw new EthereumApiException( func.name + " is defined as constant but return type is CompletableFuture. This is only for non constant functions");
                }

                if(!func.constant && !(isFutureType || isPayableType)) {
                    throw new EthereumApiException( func.name + " is not defined as constant but return type is " + method.getReturnType().getSimpleName()+ ". non constant function return type should be CompletableFuture<" + method.getReturnType().getSimpleName()+ "> instead.");
                }
            });
        }
    }

    public void addFutureConverter(final FutureConverter futureConverter) {
        futureConverters.add(futureConverter);
    }
}
