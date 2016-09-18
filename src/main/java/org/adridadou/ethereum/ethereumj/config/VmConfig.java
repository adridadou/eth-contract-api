package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * <p>
 * # structured trace
 * # is the trace being
 * # collected in the
 * # form of objects and
 * # exposed to the user
 * # in json or any other
 * # convenient form.
 * vm.structured {
 * trace = false
 * dir = vmtrace
 * compressed = true
 * initStorageLimit = 10000
 * }
 */
public class VmConfig {
    private final boolean trace;
    private final String dir;
    private final boolean compressed;
    private final int initStorageLimit;

    public VmConfig(boolean trace, String dir, boolean compressed, int initStorageLimit) {
        this.trace = trace;
        this.dir = dir;
        this.compressed = compressed;
        this.initStorageLimit = initStorageLimit;
    }
}
