package org.adridadou.ethereum;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContractReal;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.CopyOnWriteMap;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyReal implements BlockchainProxy {

    private static final long BLOCK_WAIT_LIMIT = 16;
    private final Ethereum ethereum;
    private final EthereumEventHandler eventHandler;
    private final Map<EthAccount, BigInteger> pendingTransactions = new CopyOnWriteMap<>();

    public BlockchainProxyReal(Ethereum ethereum, EthereumEventHandler eventHandler) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        eventHandler.onReady().thenAccept((b) -> ethereum.getBlockchain().flush());
    }

    @Override
    public SmartContract map(SoliditySource src, String contractName, EthAddress address, EthAccount sender) {
        CompilationResult.ContractMetadata metadata;
        try {
            metadata = compile(src, contractName);
            return mapFromAbi(new ContractAbi(metadata.abi), address, sender);

        } catch (IOException e) {
            throw new EthereumApiException("error while mapping a smart contract", e);
        }
    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractReal(abi.getAbi(), ethereum, sender, address, this);
    }

    @Override
    public CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs) {
        try {
            return createContract(code, contractName, sender, constructorArgs).thenApply(SmartContractReal::getAddress);
        } catch (IOException e) {
            throw new EthereumApiException("error while publishing " + contractName + ":", e);
        }
    }

    private CompletableFuture<SmartContractReal> createContract(SoliditySource soliditySrc, String contractName, EthAccount sender, Object... constructorArgs) throws IOException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function constructor = contract.getConstructor();
        if (constructor == null && constructorArgs.length > 0) {
            throw new EthereumApiException("No constructor with params found");
        }
        byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
        return sendTx(1, ByteUtil.merge(Hex.decode(metadata.bin), argsEncoded), sender, null)
                .thenApply(receipt -> EthAddress.of(receipt.getTransaction().getContractAddress()))
                .thenApply(address -> new SmartContractReal(metadata.abi, ethereum, sender, address, this));
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

    private BigInteger getNonce(final EthAccount account) {
        BigInteger nonce = ethereum.getRepository().getNonce(account.getAddress().address);
        return nonce.add(pendingTransactions.getOrDefault(account, BigInteger.ZERO));
    }

    public CompletableFuture<TransactionReceipt> sendTx(long value, byte[] data, EthAccount sender, EthAddress toAddress) {
        return eventHandler.onReady().thenCompose((b) -> {
            BigInteger nonce = getNonce(sender);
            Transaction tx = new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                    ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                    Optional.ofNullable(toAddress).map(addr -> addr.address).orElse(null),
                    ByteUtil.longToBytesNoLeadZeroes(value),
                    data);
            tx.sign(sender.key);
            ethereum.submitTransaction(tx);
            increasePendingTransactionCounter(sender);
            long currentBlock = eventHandler.getCurrentBlockNumber();

            Predicate<TransactionReceipt> findReceipt = (TransactionReceipt receipt) -> new ByteArrayWrapper(receipt.getTransaction().getHash()).equals(new ByteArrayWrapper(tx.getHash()));

            return CompletableFuture.supplyAsync(() -> eventHandler.observeBlocks()
                    .filter(params -> params.receipts.stream().anyMatch(findReceipt) || params.block.getNumber() > currentBlock + BLOCK_WAIT_LIMIT)
                    .map(params -> {
                        Optional<TransactionReceipt> receipt = params.receipts.stream().filter(findReceipt).findFirst();
                        decreasePendingTransactionCounter(sender);
                        return receipt.map(eventHandler::checkForErrors)
                                .<EthereumApiException>orElseThrow(() -> new EthereumApiException("the transaction has not been added to any block after waiting for " + BLOCK_WAIT_LIMIT));
                    }).toBlocking().first());
        });
    }

    @Override
    public EthereumEventHandler events() {
        return eventHandler;
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return ethereum.getRepository().isExist(address.address);
    }

    private void decreasePendingTransactionCounter(EthAccount sender) {
        pendingTransactions.put(sender, pendingTransactions.getOrDefault(sender, BigInteger.ZERO).subtract(BigInteger.ONE));
    }

    private void increasePendingTransactionCounter(EthAccount sender) {
        pendingTransactions.put(sender, pendingTransactions.getOrDefault(sender, BigInteger.ZERO).add(BigInteger.ONE));
    }
}
