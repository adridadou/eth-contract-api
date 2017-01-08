package org.adridadou.ethereum.blockchain;

import static org.adridadou.ethereum.values.EthValue.wei;
import static org.ethereum.config.blockchain.FrontierConfig.FrontierConstants;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.smartcontract.SmartContract;
import org.adridadou.ethereum.smartcontract.SmartContractTest;
import org.adridadou.ethereum.values.*;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import rx.Observable;

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
    public SmartContract mapFromAbi(ContractAbi abi, EthAddress address, EthAccount sender) {
        return new SmartContractTest(blockchain.createExistingContractFromABI(abi.getAbi(), address.address));
    }

    @Override
    public CompletableFuture<EthAddress> publish(CompiledContract contract, EthAccount sender, Object... constructorArgs) {
        return CompletableFuture.completedFuture(EthAddress.of(blockchain.submitNewContract(contract.getSource().getSource(), contract.getName(), constructorArgs).getAddress()));
    }

    @Override
    public CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount sender, EthAddress address) {
        return null;
    }

    @Override
    public CompletableFuture<EthAddress> sendTx(EthValue ethValue, EthData data, EthAccount sender) {
        return this.sendTx(ethValue, data, sender, EthAddress.empty()).thenApply(result -> EthAddress.of(result.getResult()));
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

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return SmartContractByteCode.of(new byte[0]);
    }

    @Override
    public <T> Observable<T> observeEvents(EthAddress contractAddress, String eventName, Class<T> cls) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void shutdown() {

    }


}
