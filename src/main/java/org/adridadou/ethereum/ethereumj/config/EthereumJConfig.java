package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * <p>
 * # the folder resources/genesis
 * # contains several versions of
 * # genesis configuration according
 * # to the network the peer will run on
 * genesis = frontier.json
 * <p>
 * # Blockchain settings (constants and algorithms) which are
 * # not described in the genesis file (like MINIMUM_DIFFICULTY or Mining algorithm)
 * # The possible named presets are:
 * # - main : the main network (Frontier-Homestead-...)
 * # - morden: Morden test network
 * # - testnet: Ethercamp test network
 * # - olympic: pre-Frontier Olympic network
 * # For custom network settings please refer to 'blockchain.config.class'
 * blockchain.config.name = "main"
 * <p>
 * # This is a more advanced replacement for 'blockchain.config.name'
 * # Here the exact org.ethereum.config.BlockchainForkConfig implementation
 * # class name can be specified.
 * # Only one of two options (this and above) can be defined.
 * #blockchain.config.class = "org.ethereum.config.fork.MainForkConfig"
 * <p>
 * # the time we wait to the network
 * # to approve the transaction, the
 * # transaction got approved when
 * # include into a transactions msg
 * # retrieved from the peer [seconds]
 * transaction.approve.timeout = 15
 * <p>
 * # the number of blocks that should pass
 * # before a pending transaction is removed
 * transaction.outdated.threshold = 10
 * <p>
 * # default directory where we keep
 * # basic Serpent samples relative
 * # to home.dir
 * samples.dir = samples
 * <p>
 * # this string is used to compute
 * # the address that gets the miner reward
 * coinbase.secret = monkey
 * <p>
 * # make changes to tracing options
 * # starting from certain block
 * # -1 don't make any tracing changes
 * trace.startblock = -1
 * <p>
 * # invoke vm program on
 * # message received,
 * # if the vm is not invoked
 * # the balance transfer
 * # occurs anyway  [true/false]
 * play.vm = true
 * <p>
 * # hello phrase will be included in
 * # the hello message of the peer
 * hello.phrase = Dev
 * <p>
 * # this property used
 * # mostly for debug purposes
 * # so if you don't know exactly how
 * # to apply it, leave it as [-1]
 * #
 * # ADVANCED: if we want to load a root hash
 * # for db not from the saved block chain (last block)
 * # but any manual hash this property will help.
 * # values [-1] - load from db
 * #        [hex hash 32 bytes] root hash
 * root.hash.start = null
 * <p>
 * # Key value data source values: [leveldb/redis/mapdb]
 * keyvalue.datasource = leveldb
 * <p>
 * # Redis cloud enabled flag.
 * # Allows using RedisConnection for creating cloud based data structures.
 * redis.enabled=false
 * <p>
 * record.blocks=false
 * blockchain.only=false
 * <p>
 * # Load the blocks
 * # from a rlp lines
 * # file and not for
 * # the net
 * blocks.loader=""
 * <p>
 * <p>
 * # this parameter specifies when
 * # to switch managing storage of the
 * # account on autonomous db
 * # the limit is specified in contract storage bytes
 * details.inmemory.storage.limit=1000000
 */
public class EthereumJConfig {

    private final long storageLimit;
    private final String blocksLoader;
    private final boolean blockchainOnly;
    private final boolean redisEnabled;
    private final BlockchainDataSource dataSource;
    private final String helloPhrase;
    private final boolean playVm;
    private final int traceStartBlock;
    private final String coinbaseSecret;
    private final String samplesDir;
    private final int transactionOutdatedThreshold;
    private final int transactionApproveTimeout;
    private final Class<?> configClass;
    private final String configName;
    private final String geneis;

    public EthereumJConfig(long storageLimit, String blocksLoader, boolean blockchainOnly, boolean redisEnabled, BlockchainDataSource dataSource, String helloPhrase, boolean playVm, int traceStartBlock, String coinbaseSecret, String samplesDir, int transactionOutdatedThreshold, int transactionApproveTimeout, Class<?> configClass, String configName, String geneis) {
        this.storageLimit = storageLimit;
        this.blocksLoader = blocksLoader;
        this.blockchainOnly = blockchainOnly;
        this.redisEnabled = redisEnabled;
        this.dataSource = dataSource;
        this.helloPhrase = helloPhrase;
        this.playVm = playVm;
        this.traceStartBlock = traceStartBlock;
        this.coinbaseSecret = coinbaseSecret;
        this.samplesDir = samplesDir;
        this.transactionOutdatedThreshold = transactionOutdatedThreshold;
        this.transactionApproveTimeout = transactionApproveTimeout;
        this.configClass = configClass;
        this.configName = configName;
        this.geneis = geneis;
    }

    // root.hash.start = null TODO:implement that. Not clear how it works
}
