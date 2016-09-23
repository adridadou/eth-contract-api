package org.adridadou.ethereum;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Predicate;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.RealSmartContract;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import rx.Observable;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyReal implements BlockchainProxy {

    private static final long BLOCK_WAIT_LIMIT = 16;
    private final Ethereum ethereum;
    private final EthereumEventHandler eventHandler;

    public BlockchainProxyReal(Ethereum ethereum, EthereumEventHandler eventHandler) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        eventHandler.onReady().doOnCompleted(() -> ethereum.getBlockchain().flush());
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
        return new RealSmartContract(abi.getAbi(), ethereum, sender, address, this);
    }

    @Override
    public Observable<EthAddress> publish(SoliditySource code, String contractName, ECKey sender, Object... constructorArgs) {
        try {
            return createContract(code, contractName, sender, constructorArgs).map(RealSmartContract::getAddress);
        } catch (IOException e) {
            throw new EthereumApiException("error while publishing " + contractName + ":", e);
        }
    }

    private Observable<RealSmartContract> createContract(SoliditySource soliditySrc, String contractName, ECKey sender, Object... constructorArgs) throws IOException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(1, ByteUtil.merge(Hex.decode(metadata.bin), argsEncoded), sender, null)
                .map(receipt -> EthAddress.of(receipt.getTransaction().getContractAddress()))
                .map(address -> new RealSmartContract(metadata.abi, ethereum, sender, address, this));
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

    public Observable<TransactionReceipt> sendTx(long value, byte[] data, ECKey sender, String toAddress) {
        return eventHandler.onReady().flatMap((b) -> {
            BigInteger nonce = ethereum.getRepository().getNonce(sender.getAddress());
            Transaction tx = new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                    ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                    toAddress == null ? null : Hex.decode(toAddress),
                    ByteUtil.longToBytesNoLeadZeroes(value),
                    data);
            tx.sign(sender);
            ethereum.submitTransaction(tx);

            long currentBlock = eventHandler.getCurrentBlockNumber();

            Predicate<TransactionReceipt> findReceipt = (TransactionReceipt receipt) -> new ByteArrayWrapper(receipt.getTransaction().getHash()).equals(new ByteArrayWrapper(tx.getHash()));

            return eventHandler.observeBlocks()
                    .filter(params -> params.receipts.stream().anyMatch(findReceipt) || params.block.getNumber() > currentBlock + BLOCK_WAIT_LIMIT)
                    .map(params -> {
                        Optional<TransactionReceipt> receipt = params.receipts.stream().filter(findReceipt).findFirst();
                        return receipt.map(eventHandler::checkForErrors)
                                .<EthereumApiException>orElseThrow(() -> new EthereumApiException("the transaction has not been added to any block after waiting for " + BLOCK_WAIT_LIMIT));
                    }).first();
        });
    }

    @Override
    public EthereumEventHandler events() {
        return eventHandler;
    }


}
