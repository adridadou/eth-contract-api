package org.adridadou.ethereum.values;

import java.io.*;

/**
 * Created by davidroon on 21.02.17.
 * This code is released under Apache 2 license
 */
public class SoliditySourceFile implements SoliditySource<File>{

    private final File source;

    public SoliditySourceFile(File source) {
        this.source = source;
    }

    @Override
    public File getSource() {
        return source;
    }

    public static SoliditySourceFile from(File file) {
        return new SoliditySourceFile(file);

    }

    @Override
    public String toString() {
        return "source:" + source;
    }
}
