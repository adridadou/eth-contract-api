package org.adridadou.ethereum;

import org.adridadou.ethereum.smartcontract.RealSmartContract;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.*;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import rx.Observable;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyImpl implements BlockchainProxy {

    private final Ethereum ethereum;
    private final EthereumEventHandler ethereumListener;

    public BlockchainProxyImpl(Ethereum ethereum, EthereumEventHandler ethereumListener) {
        this.ethereum = ethereum;
        this.ethereumListener = ethereumListener;
    }

    @Override
    public SmartContract map(String src, String contractName, EthAddress address, ECKey sender) {
        CompilationResult.ContractMetadata metadata;
        try {
            metadata = compile(src, contractName);
            return mapFromAbi(metadata.abi, address, sender);

        } catch (IOException e) {
            throw new EthereumApiException("error while mapping a smart contract");
        }
    }

    @Override
    public SmartContract mapFromAbi(String abi, EthAddress address, ECKey sender) {
        RealSmartContract sc = new RealSmartContract(abi, ethereum, sender, this);
        sc.setAddress(address);
        return sc;
    }

    @Override
    public Observable<EthAddress> publish(String code, String contractName, ECKey sender) {
        try {
            return createContract(code, contractName, sender).map(RealSmartContract::getAddress);
        } catch (IOException | InterruptedException e) {
            throw new EthereumApiException("error while publishing the smart contract");
        }
    }

    private Observable<RealSmartContract> createContract(String soliditySrc, String contractName, ECKey sender) throws IOException, InterruptedException {
        CompilationResult.ContractMetadata metadata = compile(soliditySrc, contractName);
        return sendTx(1, Hex.decode(metadata.bin), sender).map(receipt -> {
            EthAddress contractAddress = EthAddress.of(receipt.getTransaction().getContractAddress());

            RealSmartContract newContract = new RealSmartContract(metadata.abi, ethereum, sender, this);
            newContract.setAddress(contractAddress);
            return newContract;
        });

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

    public Observable<TransactionReceipt> sendTx(long value, byte[] data, ECKey sender) throws InterruptedException {
        return ethereumListener.getSync().flatMap((b) -> {
            BigInteger nonce = ethereum.getRepository().getNonce(sender.getAddress());
            Transaction tx = new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                    ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                    new byte[0],
                    ByteUtil.longToBytesNoLeadZeroes(value),
                    data);
            tx.sign(sender);
            ethereum.submitTransaction(tx);

            return ethereumListener.registerTx(tx);
        });
    }
}
