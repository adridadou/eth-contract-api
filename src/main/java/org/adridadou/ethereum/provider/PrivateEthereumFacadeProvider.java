package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.EthereumProxyEthereumJ;
import org.adridadou.ethereum.blockchain.EthereumJReal;
import org.adridadou.ethereum.blockchain.Ethereumj;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.config.ChainId;
import org.adridadou.ethereum.values.config.DatabaseDirectory;
import org.adridadou.exception.EthereumApiException;
import org.apache.commons.io.FileUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListener;
import org.ethereum.mine.Ethash;
import org.ethereum.mine.MinerListener;
import org.ethereum.samples.BasicSample;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by davidroon on 20.11.16.
 * This code is released under Apache 2 license
 */
public class PrivateEthereumFacadeProvider {
    private final Logger log = LoggerFactory.getLogger(PrivateEthereumFacadeProvider.class);
    private final EthAccount mainAccount = AccountProvider.from("cow");

    public EthereumFacade create(final PrivateNetworkConfig config) {
        final boolean dagCached = new File("cachedDag/mine-dag.dat").exists();
        if (config.isResetPrivateBlockchain()) {
            deleteFolder(new File(config.getDbName()), true);
        }

        if (dagCached) {
            new File(config.getDbName()).mkdirs();
            try {
                FileUtils.copyFile(new File("cachedDag/mine-dag.dat"), new File(config.getDbName() + "/mine-dag.dat"));
                FileUtils.copyFile(new File("cachedDag/mine-dag-light.dat"), new File(config.getDbName() + "/mine-dag-light.dat"));
            } catch (IOException e) {
                throw new EthereumApiException("error while copying dag files", e);
            }
        }

        MinerConfig.dbName = config.getDbName();
        Ethereum ethereum = EthereumFactory.createEthereum(MinerConfig.class);
        Ethereumj ethereumj = new EthereumJReal(ethereum, ChainId.id(1));
        ethereum.initSyncing();

        if (!dagCached) {
            try {
                new File("cachedDag").mkdirs();
                FileUtils.copyFile(new File(config.getDbName() + "/mine-dag.dat"), new File("cachedDag/mine-dag.dat"));
                FileUtils.copyFile(new File(config.getDbName() + "/mine-dag-light.dat"), new File("cachedDag/mine-dag-light.dat"));
            } catch (IOException e) {
                log.warn("couldn't copy files: " + e.getMessage());
            }
        }

        EthereumEventHandler ethereumListener = new EthereumEventHandler(ethereumj);
        InputTypeHandler inputTypeHandler = new InputTypeHandler();
        OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
        final EthereumFacade facade = new EthereumFacade(new EthereumProxyEthereumJ(ethereumj, ethereumListener, inputTypeHandler, outputTypeHandler),inputTypeHandler, outputTypeHandler, SwarmService.from(SwarmService.PUBLIC_HOST), SolidityCompiler.getInstance());

        //This event does not trigger when you are the miner
        ethereumListener.onSyncDone(EthereumListener.SyncState.COMPLETE);
        facade.events().onReady().thenAccept((b) -> config.getInitialBalances().entrySet().stream()
                .map(entry -> facade.sendEther(mainAccount, entry.getKey().getAddress(), entry.getValue()))
                .forEach(result -> {
                    try {
                        result.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new EthereumApiException("error while setting the initial balances");
                    }
                })
        );
        return facade;
    }

    private void deleteFolder(File folder, final boolean root) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f, false);
                } else {
                    f.delete();
                }
            }
        }
        if (!root) {
            folder.delete();
        }
    }

    /**
     * Spring configuration class for the Miner peer
     */
    private static class MinerConfig {

        public static String dbName = "sampleDB";

        public static String config() {
            // no need for discovery in that small network
            return EthereumJConfigs.privateMiner()
                    .dbDirectory(DatabaseDirectory.db(dbName))
                    .listenPort(55555)
                    .build().toString();
        }

        @Bean
        public MinerNode node() {
            return new MinerNode();
        }

        /**
         * Instead of supplying properties via extendConfig file for the peer
         * we are substituting the corresponding bean which returns required
         * extendConfig for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties(ConfigFactory.empty(), PrivateEthereumFacadeProvider.class.getClassLoader());
            props.overrideParams(ConfigFactory.parseString(config().replaceAll("'", "\"")));
            return props;
        }
    }

    /**
     * Miner bean, which just start a miner upon creation and prints miner events
     */
    static class MinerNode extends BasicSample implements MinerListener {
        public MinerNode() {
            // peers need different loggers
            super("sampleMiner");
        }

        // overriding run() method since we don't need to wait for any discovery,
        // networking or sync events
        @Override
        public void run() {
            if (config.isMineFullDataset()) {
                logger.info("Generating Full Dataset (may take up to 10 min if not cached)...");
                // calling this just for indication of the dataset generation
                // basically this is not required
                Ethash ethash = Ethash.getForBlock(config, ethereum.getBlockchain().getBestBlock().getNumber());
                ethash.getFullDataset();
                logger.info("Full dataset generated (loaded).");
            }
            ethereum.getBlockMiner().addListener(this);
            ethereum.getBlockMiner().startMining();
        }

        @Override
        public void miningStarted() {
            logger.info("Miner started");
        }

        @Override
        public void miningStopped() {
            logger.info("Miner stopped");
        }

        @Override
        public void blockMiningStarted(Block block) {
            logger.info("Start mining block: " + block.getShortDescr());
        }

        @Override
        public void blockMined(Block block) {
            logger.info("Block mined! : \n" + block);
        }

        @Override
        public void blockMiningCanceled(Block block) {
            logger.info("Cancel mining block: " + block.getShortDescr());
        }
    }
}
