package org.adridadou.ethereum.values;

import org.adridadou.ethereum.EthereumFacade;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by davidroon on 21.02.17.
 * This code is released under Apache 2 license
 */
public class SoliditySourceString implements SoliditySource<String>{

    private final String source;

    public SoliditySourceString(String source) {
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

    public static SoliditySourceString from(File file) {
        try {
            return from(new FileInputStream(file));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static SoliditySourceString from(InputStream file) {
        try {
            return new SoliditySourceString(IOUtils.toString(file, EthereumFacade.CHARSET));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static SoliditySourceString from(String source) {
        return new SoliditySourceString(source);
    }

    @Override
    public String toString() {
        return "source:" + source;
    }
}
