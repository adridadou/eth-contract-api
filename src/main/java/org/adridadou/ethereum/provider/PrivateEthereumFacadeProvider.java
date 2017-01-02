package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.BlockchainProxyReal;
import org.adridadou.ethereum.handler.EthereumEventHandler;
import org.adridadou.ethereum.handler.OnBlockHandler;
import org.adridadou.ethereum.handler.OnTransactionHandler;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.EthAccount;
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
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by davidroon on 20.11.16.
 * This code is released under Apache 2 license
 */
public class PrivateEthereumFacadeProvider {
    private final EthAccount mainAccount = AccountProvider.from("cow");

    /**
     * Spring configuration class for the Miner peer
     */
    private static class MinerConfig {

        public static String dbName = "sampleDB";

        public static String config() {
            // no need for discovery in that small network
            return EthereumJConfigs.privateMiner()
                    .dbDirectory(DatabaseDirectory.db(dbName))
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

    private static class RegularConfig {
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties(ConfigFactory.empty(), PrivateEthereumFacadeProvider.class.getClassLoader());
            props.overrideParams(ConfigFactory.parseString("peer.discovery.enabled = false \npeer.listen.port = 30336 \npeer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \npeer.networkId = 555 \npeer.active = [    { url = \'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335\' }] \nsync.enabled = true \ngenesis = private-genesis.json \ndatabase.dir = sampleDB-2 \n".replaceAll("\'", "\"")));
            return props;
        }
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
        ethereum.initSyncing();

        if (!dagCached) {
            try {
                FileUtils.copyFile(new File(config.getDbName() + "/mine-dag.dat"), new File("cachedDag/mine-dag.dat"));
                FileUtils.copyFile(new File(config.getDbName() + "/mine-dag-light.dat"), new File("cachedDag/mine-dag-light.dat"));
            } catch (IOException e) {
                throw new EthereumApiException("error while copying dag files to dagCached", e);
            }
        }

        EthereumEventHandler ethereumListener = new EthereumEventHandler(ethereum, new OnBlockHandler(), new OnTransactionHandler());
        final EthereumFacade facade = new EthereumFacade(new BlockchainProxyReal(ethereum, ethereumListener, SwarmService.from(SwarmService.PUBLIC_HOST)));

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

}
