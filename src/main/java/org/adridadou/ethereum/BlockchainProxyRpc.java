package org.adridadou.ethereum;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractRpc;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthCall;
import org.web3j.protocol.core.methods.request.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import rx.Observable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyRpc implements BlockchainProxy {

    private final Web3j web3j;

    public BlockchainProxyRpc(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public SmartContract map(SoliditySource src, String contractName, EthAddress address, ECKey sender) {
        CompilationResult.ContractMetadata metadata;
        try {
            metadata = compile(src, contractName);
            return mapFromAbi(new ContractAbi(metadata.abi), address, sender);

        } catch (IOException e) {
            throw new EthereumApiException("error while mapping a smart contract", e);
        }
    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, ECKey sender) {
        return new SmartContractRpc(abi.getAbi(), web3j, sender, address, this);
    }

    @Override
    public Observable<EthAddress> publish(SoliditySource code, String contractName, ECKey sender, Object... constructorArgs) {
        try {
            return createContract(code, contractName, sender, constructorArgs).map(SmartContractRpc::getAddress);
        } catch (IOException e) {
            throw new EthereumApiException("error while publishing " + contractName + ":", e);
        }
    }

    private Observable<SmartContractRpc> createContract(SoliditySource soliditySrc, String contractName, ECKey sender, Object... constructorArgs) throws IOException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(1, ByteUtil.merge(Hex.decode(metadata.bin), argsEncoded), sender)
                .map(address -> new SmartContractRpc(metadata.abi, web3j, sender, address, this));
    }

    private CompilationResult.ContractMetadata compile(SoliditySource src, String contractName) throws IOException {
        SolidityCompiler.Result result = SolidityCompiler.compile(src.getSource().getBytes(EthereumFacade.CHARSET), true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (result.isFailed()) {
            throw new EthereumApiException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.contracts.isEmpty()) {
            throw new EthereumApiException("Compilation failed, no contracts returned:\n" + result.errors);
        }
        CompilationResult.ContractMetadata metadata = res.contracts.get(contractName);
        if (metadata != null && (metadata.bin == null || metadata.bin.isEmpty())) {
            throw new EthereumApiException("Compilation failed, no binary returned:\n" + result.errors);
        }
        return metadata;
    }

    private CompletableFuture<EthGetTransactionReceipt.TransactionReceipt> getTransactionReceipt(final String txHash) {
        return web3j.ethGetTransactionReceipt(txHash).sendAsync()
                .thenCompose(result -> result.getTransactionReceipt()
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(() -> {
                            try {
                                Thread.sleep(1000); //TODO: this is hacky, we should have a better way to do that
                            } catch (InterruptedException e) {
                                throw new EthereumApiException(e.getMessage(), e);
                            }
                            return getTransactionReceipt(txHash);
                        }));
    }

    public Observable<EthExecutionResult> sendTx(long value, byte[] data, ECKey sender, EthAddress toAddress) {
        final EthAddress senderAddress = EthAddress.of(sender.getAddress());
        return Observable.from(web3j.ethEstimateGas(new EthCall(toAddress.toString(), Hex.toHexString(data)))
                .sendAsync()
                .thenCompose(gas -> web3j.ethSendTransaction(new EthSendTransaction(senderAddress.toString(), toAddress.toString(), gas.getAmountUsed(), Hex.toHexString(data))).sendAsync())
                .thenCompose(result -> getTransactionReceipt(result.getTransactionHash()))
                .thenApply(receipt -> new EthExecutionResult(null))
        );
    }

    @Override
    public Observable<EthAddress> sendTx(long value, byte[] data, ECKey sender) {
        final EthAddress senderAddress = EthAddress.of(sender.getAddress());
        return Observable.from(web3j.ethEstimateGas(new EthCall(null, Hex.toHexString(data)))
                .sendAsync()
                .thenCompose(gas -> web3j.ethSendTransaction(new EthSendTransaction(senderAddress.toString(), null, gas.getAmountUsed(), Hex.toHexString(data))).sendAsync())
                .thenCompose(result -> getTransactionReceipt(result.getTransactionHash()))
                .thenApply(receipt -> EthAddress.of(receipt.getContractAddress().orElse(null)))
        );
    }

    @Override
    public EthereumEventHandler events() {
        return null;
    }

    @Override
    public boolean addressExists(EthAddress address) {
        throw new EthereumApiException("addressExists is not implemented for RPC");
    }


}
