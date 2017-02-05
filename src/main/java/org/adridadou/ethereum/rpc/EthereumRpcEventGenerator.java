package org.adridadou.ethereum.rpc;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.event.OnBlockParameters;
import org.adridadou.ethereum.event.TransactionReceipt;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.adridadou.ethereum.values.EthHash;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 30.01.17.
 */
public class EthereumRpcEventGenerator {
    private final List<EthereumEventHandler> ethereumEventHandlers = new ArrayList<>();
    private final Web3JFacade web3JFacade;

    public EthereumRpcEventGenerator(Web3JFacade web3JFacade) {
        this.web3JFacade = web3JFacade;
        web3JFacade.observeBlocks().subscribe(this::observeBlocks);
    }

    private void observeBlocks(EthBlock ethBlock) {
        ethBlock.getBlock().ifPresent(block -> {
            List<TransactionReceipt> txs = block.getTransactions().stream()
                    .map(tx -> toReceipt((EthBlock.TransactionObject)tx.get()))
                    .collect(Collectors.toList());

            OnBlockParameters param = new OnBlockParameters(block.getNumber().longValue(), txs);
            ethereumEventHandlers.forEach(handler -> handler.onBlock(param));
        });
    }

    private TransactionReceipt toReceipt(EthBlock.TransactionObject tx) {
        org.web3j.protocol.core.methods.response.TransactionReceipt receipt = web3JFacade.getReceipt(EthHash.of(tx.getHash()));
        boolean successful = !receipt.getGasUsed().equals(tx.getGas());
        String error = "";
        if(!successful) {
            error = "Error fromSeed RPC, all the gas was used";
        }
        return new TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getFrom()),EthAddress.of(tx.getTo()), EthAddress.of(receipt.getContractAddress()), error, EthData.empty(), successful);
    }

    public void addListener(EthereumEventHandler ethereumEventHandler) {
        this.ethereumEventHandlers.add(ethereumEventHandler);
    }
}
