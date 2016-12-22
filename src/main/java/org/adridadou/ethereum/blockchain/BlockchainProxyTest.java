package org.adridadou.ethereum.blockchain;

import static org.adridadou.ethereum.values.EthValue.wei;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractTest;
import org.adridadou.ethereum.values.ContractAbi;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthExecutionResult;
import org.adridadou.ethereum.values.EthValue;
import org.adridadou.ethereum.values.SoliditySource;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.FrontierConfig.FrontierConstants;
import org.ethereum.util.blockchain.StandaloneBlockchain;

/**
 * Created by davidroon on 08.04.16.
 * This code is released under Apache 2 license
 */
public class BlockchainProxyTest implements BlockchainProxy {
    private final StandaloneBlockchain blockchain;

    public BlockchainProxyTest() {

        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        blockchain = new StandaloneBlockchain();
        blockchain.withAutoblock(true);
    }

    @Override
    public SmartContract map(SoliditySource src, String contractName, EthAddress address, EthAccount sender) {
        return new SmartContractTest(blockchain.createExistingContractFromSrc(src.getSource(), contractName, address.address));

    }

    @Override
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractTest(blockchain.createExistingContractFromABI(abi.getAbi(), address.address));
    }

    @Override
    public CompletableFuture<EthAddress> publish(SoliditySource code, String contractName, EthAccount sender, Object... constructorArgs) {
        if(code.isCompiled()){
//        	ContractMetadata contractMetadata = code.getContractData().contracts.get(contractName);
//        	String bin = contractMetadata.bin;
//        	Contract contract = new CallTransaction.Contract(contractMetadata.abi);
//            CallTransaction.Function constructor = contract.contract.getConstructor();
//            if (constructor == null && constructorArgs.length > 0) {
//                throw new RuntimeException("No constructor with params found");
//            }
//            byte[] argsEncoded = constructor == null ? new byte[0] : constructor.encodeArguments(constructorArgs);
//            submitNewTx(new PendingTx(new byte[0], BigInteger.ZERO,
//                    ByteUtil.merge(Hex.decode(bin), argsEncoded), contract, null, new TransactionResult()));
//            
////            Transaction tx = new  Transaction(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v, chainId);
//			
//            BigInteger nonce = getNonce(sender.getAddress());
//            Transaction tx = new Transaction(
//                    ByteUtil.bigIntegerToBytes(nonce),
//                    ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
//                    ByteUtil.longToBytesNoLeadZeroes(3_000_000),
//                    toAddress.address,
//                    ByteUtil.longToBytesNoLeadZeroes(value.inWei().longValue()),
//                    ByteUtil.merge(Hex.decode(bin),
//                    blockchain.getChainIdForNextBlock());
//            tx.sign(sender.key);
//            
//
//            
//            
//            blockchain.submitTransaction(tx );
        	
//        	blockchain.submitNewContract(soliditySrc, contractName, constructorArgs)
        }
    	
    	return CompletableFuture.completedFuture(EthAddress.of(blockchain.submitNewContract(code.getSource(), contractName, constructorArgs).getAddress()));
    }

    @Override
    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address) {
        return null;
    }

    @Override
    public CompletableFuture<EthAddress> sendTx(EthValue ethValue, EthData data, EthAccount sender) {
        return this.sendTx(ethValue, data, sender, null).thenApply(result -> EthAddress.of(result.getResult()));
    }

    @Override
    public EthereumEventHandler events() {
        return null;
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return true;
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return wei(0);
    }

    @Override
    public BigInteger getNonce(EthAddress address) {
        return BigInteger.ONE;
    }

}
