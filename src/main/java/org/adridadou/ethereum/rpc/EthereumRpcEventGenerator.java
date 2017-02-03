package org.adridadou.ethereum.rpc;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.event.OnBlockParameters;
import org.adridadou.ethereum.event.TransactionReceipt;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.EthData;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 30.01.17.
 */
public class EthereumRpcEventGenerator {
    private final List<EthereumEventHandler> ethereumEventHandlers = new ArrayList<>();

    public EthereumRpcEventGenerator(Web3JFacade web3JFacade) {
        web3JFacade.observeBlocks().subscribe(this::observeBlocks);
    }

    private void observeBlocks(EthBlock ethBlock) {
        ethBlock.getBlock().ifPresent(block -> {
            System.out.println("**** block received:" + block.getNumber());
            List<TransactionReceipt> txs = block.getTransactions().stream()
                    .map(tx -> toReceipt((EthBlock.TransactionObject)tx.get()))
                    .collect(Collectors.toList());

            new OnBlockParameters(block.getNumber().longValue(),txs);
            ethereumEventHandlers
                    .forEach(handler -> this.handleNewBlock(handler, block));
        });
    }

    private void handleNewBlock(EthereumEventHandler eventHandler, EthBlock.Block block) {
        List<TransactionReceipt> txs = block.getTransactions().stream()
                .map(result -> ((EthBlock.TransactionObject)result.get()))
                .map(this::toReceipt)
                .collect(Collectors.toList());

        eventHandler.onBlock(new OnBlockParameters(block.getNumber().longValue(), txs));
    }


    private TransactionReceipt toReceipt(EthBlock.TransactionObject transactionObject) {
        //TODO: can I figure out if the transaction was successful or not?
        return new TransactionReceipt(EthData.of(transactionObject.getHash()), EthAddress.of(transactionObject.getFrom()),EthAddress.of(transactionObject.getTo()),"", EthData.empty(), true);
    }

    public void addListener(EthereumEventHandler ethereumEventHandler) {
        this.ethereumEventHandlers.add(ethereumEventHandler);
    }
}
