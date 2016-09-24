package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 *
 * database {
 * # place to save physical storage files
 * # can be either absolute or relative path
 * dir = database
 *
 * # every time the application starts
 * # the existing database will be
 * # destroyed and all the data will be
 * # downloaded from peers again [true/false]
 * reset = false
 *
 * # handling incompatible database version:
 * #  * EXIT   - (default) show error in std out and exit by throwing Error
 * #  * RESET  - clear database directory and continue working
 * #  * IGNORE - continue working regardless possible issues
 * # @since 1.4.0
 * incompatibleDatabaseBehavior = EXIT
 *
 * # controls state database pruning
 * # pruned state consumes much less disk space (e.g. 50G full and 1G pruned)
 * # but the state can be restored only within last [maxDepth] blocks, all older
 * # states are lost
 * prune {
 * enabled = true
 *
 * # controls how much last block states are not pruned
 * # it is not recommneded to set this value below 192
 * # as it can prevent rebranching from long fork chains
 * maxDepth = 192
 * }
 * }
 */
public class DatabaseConfig {

    private final int maxDepth;
    private final boolean enabled;
    private final DatabaseErrorBehavior errorBehavior;
    private final boolean reset;
    private final String dir;

    public DatabaseConfig(int maxDepth, boolean enabled, DatabaseErrorBehavior errorBehavior, boolean reset, String dir) {
        this.maxDepth = maxDepth;
        this.enabled = enabled;
        this.errorBehavior = errorBehavior;
        this.reset = reset;
        this.dir = dir;
    }
}
