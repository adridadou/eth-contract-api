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
	private final String rawJson;

    
    public SoliditySource(String source) {
        this.source = source;
        this.rawJson = null;
    }

    public SoliditySource(String source, String rawJson) throws IOException {
		this.source = source;
        this.rawJson = rawJson; 
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

    public static SoliditySource from(String source) {
        return new SoliditySource(source);
    }

    public static  SoliditySource fromRawJson(String rawJson) throws IOException {
		return new SoliditySource(null,rawJson);
	}
    
    public static  SoliditySource fromRawJson(File rawJson) throws IOException {
		return fromRawJson(new FileInputStream(rawJson));
	}
    
    public static  SoliditySource fromRawJson(InputStream rawJson) throws IOException {
        try {
            return new SoliditySource(null,IOUtils.toString(rawJson, EthereumFacade.CHARSET));
        } catch (IOException e) {
            throw new IOError(e);
        }
	}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoliditySource that = (SoliditySource) o;

        return source != null ? source.equals(that.source) : that.source == null 
        	&& rawJson != null ? rawJson.equals(that.rawJson) : that.rawJson == null; 

    }

    @Override
    public int hashCode() {
    	int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (rawJson != null ? rawJson.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "source:" + source;
    }
	
	public boolean isCompiled() {
		return rawJson!=null;
	}

	public String getRawJson() {
		return rawJson;
	}
}
