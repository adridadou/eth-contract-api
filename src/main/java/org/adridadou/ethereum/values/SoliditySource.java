package org.adridadou.ethereum.values;

import org.adridadou.ethereum.EthereumFacade;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public class SoliditySource {
    private final String source;

    public SoliditySource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public static SoliditySource from(File file) {
        try {
            return from(new FileInputStream(file));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static SoliditySource from(InputStream file) {
        try {
            return new SoliditySource(IOUtils.toString(file, EthereumFacade.CHARSET));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoliditySource that = (SoliditySource) o;

        return source != null ? source.equals(that.source) : that.source == null;

    }

    @Override
    public int hashCode() {
        return source != null ? source.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "source:" + source;
    }
}
