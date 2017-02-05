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
            List<TransactionReceipt> txs = block.getTransactions().stream()
                    .map(tx -> toReceipt((EthBlock.TransactionObject)tx.get()))
                    .collect(Collectors.toList());

            OnBlockParameters param = new OnBlockParameters(block.getNumber().longValue(), txs);
            ethereumEventHandlers.forEach(handler -> handler.onBlock(param));
        });
    }

    private TransactionReceipt toReceipt(EthBlock.TransactionObject transactionObject) {
        //TODO: can I figure out if the transaction was successful or not?

        return new TransactionReceipt(EthData.of(transactionObject.getHash()), EthAddress.of(transactionObject.getFrom()),EthAddress.of(transactionObject.getTo()),"", EthData.empty(), true);
    }

    public void addListener(EthereumEventHandler ethereumEventHandler) {
        this.ethereumEventHandlers.add(ethereumEventHandler);
    }
}
