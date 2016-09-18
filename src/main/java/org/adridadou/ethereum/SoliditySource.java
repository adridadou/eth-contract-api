package org.adridadou.ethereum;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;

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
            return new SoliditySource(IOUtils.toString(new FileInputStream(file), EthereumFacade.CHARSET));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
}
