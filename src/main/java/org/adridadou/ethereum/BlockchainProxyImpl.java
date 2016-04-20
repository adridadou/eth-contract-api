package org.adridadou.ethereum;

import org.adridadou.ethereum.smartcontract.SolidityContractImpl;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.SolidityContract;
import org.spongycastle.util.encoders.Hex;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyImpl implements BlockchainProxy {

    private final Ethereum ethereum;
    private final ECKey sender;
    private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<>());

    @Inject
    public BlockchainProxyImpl(Ethereum ethereum, ECKey sender) {
        this.ethereum = ethereum;
        this.sender = sender;
    }

    @Override
    public SolidityContract map(String src, byte[] address) {
        return null;
    }

    @Override
    public EthAddress publish(String code) {
        try {
            SolidityContract contract = createContract(code);
            return EthAddress.of(contract.getAddress());
        } catch (IOException | InterruptedException e) {
            throw new EthereumApiException("error while publishing the smart contract");
        }
    }


    private SolidityContract createContract(String soliditySrc) throws IOException, InterruptedException {
        SolidityCompiler.Result result = SolidityCompiler.compile(soliditySrc.getBytes(), true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (result.isFailed()) {
            throw new RuntimeException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.contracts.isEmpty()) {
            throw new RuntimeException("Compilation failed, no contracts returned:\n" + result.errors);
        }
        CompilationResult.ContractMetadata metadata = res.contracts.values().iterator().next();
        if (metadata.bin == null || metadata.bin.isEmpty()) {
            throw new RuntimeException("Compilation failed, no binary returned:\n" + result.errors);
        }

        TransactionReceipt receipt = sendTxAndWait(new byte[0], Hex.decode(metadata.bin));

        byte[] contractAddress = receipt.getTransaction().getContractAddress();

        SolidityContractImpl newContract = new SolidityContractImpl(metadata, ethereum, sender);
        newContract.setAddress(contractAddress);
        return newContract;

    }

    private TransactionReceipt sendTxAndWait(byte[] receiveAddress, byte[] data) throws InterruptedException {
        BigInteger nonce = ethereum.getRepository().getNonce(sender.getAddress());
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                receiveAddress,
                ByteUtil.longToBytesNoLeadZeroes(1),
                data);
        tx.sign(sender.getPrivKeyBytes());
        ethereum.submitTransaction(tx);

        return waitForTx(tx.getHash());
    }

    private TransactionReceipt waitForTx(byte[] txHash) throws InterruptedException {
        ByteArrayWrapper txHashW = new ByteArrayWrapper(txHash);
        txWaiters.put(txHashW, null);
        long startBlock = ethereum.getBlockchain().getBestBlock().getNumber();
        while (true) {
            TransactionReceipt receipt = txWaiters.get(txHashW);
            if (receipt != null) {
                return receipt;
            } else {
                long curBlock = ethereum.getBlockchain().getBestBlock().getNumber();
                if (curBlock > startBlock + 16) {
                    throw new RuntimeException("The transaction was not included during last 16 blocks: " + txHashW.toString().substring(0, 8));
                }
            }
            synchronized (this) {
                wait(20000);
            }
        }
    }
}
