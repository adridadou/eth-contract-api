package org.adridadou.ethereum.ethereumj.config;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 * <p>
 * dump {
 * # for testing purposes
 * # all the state will be dumped
 * # in JSON form to [dump.dir]
 * # if [dump.full] = true
 * # possible values [true/false]
 * full = false
 * dir = dmp
 * <p>
 * # This defines the vmtrace dump
 * # to the console and the style
 * # -1 for no block trace
 * # styles: [pretty/standard+] (default: standard+)
 * block = -1
 * style = pretty
 * <p>
 * # clean the dump dir each start
 * clean.on.restart = true
 * }
 */
public class DumpConfig {
    private final boolean cleanOnRestart;
    private final VmTraceDumpStyle style;
    private final int block;
    private final String dir;
    private final boolean full;

    public DumpConfig(boolean cleanOnRestart, VmTraceDumpStyle style, int block, String dir, boolean full) {
        this.cleanOnRestart = cleanOnRestart;
        this.style = style;
        this.block = block;
        this.dir = dir;
        this.full = full;
    }

    public boolean isCleanOnRestart() {
        return cleanOnRestart;
    }

    public VmTraceDumpStyle getStyle() {
        return style;
    }

    public int getBlock() {
        return block;
    }

    public String getDir() {
        return dir;
    }

    public boolean isFull() {
        return full;
    }
}
