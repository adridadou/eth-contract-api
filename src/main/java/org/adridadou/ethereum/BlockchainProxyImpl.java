package org.adridadou.ethereum;

import org.adridadou.ethereum.smartcontract.SolidityContractImpl;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.SolidityContract;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyImpl implements BlockchainProxy {

    private final Ethereum ethereum;
    private final ECKey sender;
    private final EthereumListenerImpl ethereumListener;

    public BlockchainProxyImpl(Ethereum ethereum, ECKey sender, EthereumListenerImpl ethereumListener) {
        this.ethereum = ethereum;
        this.sender = sender;
        this.ethereumListener = ethereumListener;
    }

    @Override
    public SolidityContract map(String src, String contractName, EthAddress address) {
        CompilationResult.ContractMetadata metadata;
        try {
            metadata = compile(src, contractName);
            return mapFromAbi(metadata.abi, address);

        } catch (IOException e) {
            throw new EthereumApiException("error while mapping a smart contract");
        }
    }

    @Override
    public SolidityContract mapFromAbi(String abi, EthAddress address) {
        SolidityContractImpl sc = new SolidityContractImpl(abi, ethereum, ethereumListener, sender);
        sc.setAddress(address);
        return sc;
    }

    @Override
    public EthAddress publish(String code, String contractName) {
        try {
            SolidityContract contract = createContract(code, contractName);
            return EthAddress.of(contract.getAddress());
        } catch (IOException | InterruptedException e) {
            throw new EthereumApiException("error while publishing the smart contract");
        }
    }

    @Override
    public boolean isSyncDone() {
        return ethereumListener.isSynced();
    }

    @Override
    public EthAddress getSenderAddress() {
        return EthAddress.of(sender.getAddress());
    }

    @Override
    public long getCurrentBlockNumber() {
        return ethereum.getBlockchain().getBestBlock().getNumber();
    }


    private SolidityContract createContract(String soliditySrc, String contractName) throws IOException, InterruptedException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        TransactionReceipt receipt = sendTxAndWait(new byte[0], Hex.decode(metadata.bin));

        EthAddress contractAddress = EthAddress.of(receipt.getTransaction().getContractAddress());

        SolidityContractImpl newContract = new SolidityContractImpl(metadata.abi, ethereum, ethereumListener, sender);
        newContract.setAddress(contractAddress);
        return newContract;
    }

    private CompilationResult.ContractMetadata compile(String src, String contractName) throws IOException {
        SolidityCompiler.Result result = SolidityCompiler.compile(src.getBytes(), true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (result.isFailed()) {
            throw new RuntimeException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.contracts.isEmpty()) {
            throw new RuntimeException("Compilation failed, no contracts returned:\n" + result.errors);
        }
        CompilationResult.ContractMetadata metadata = res.contracts.get(contractName);
        if (metadata != null && (metadata.bin == null || metadata.bin.isEmpty())) {
            throw new RuntimeException("Compilation failed, no binary returned:\n" + result.errors);
        }
        return metadata;
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
        tx.sign(sender);
        ethereum.submitTransaction(tx);

        return ethereumListener.waitForTx(tx.getHash());
    }
}
