package org.adridadou.ethereum.values;

import org.adridadou.ethereum.EthereumFacade;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.CompilationResult.ContractMetadata;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Collections;
import java.util.Map;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public class SoliditySource {
    private final String source;
	private ContractData contractData;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ContractData{
    public Map<String, ContractMetadata> contracts;
    public String version;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractMetadata {
        public String abi;
        public String bin;
        public String solInterface;

        public String getInterface() {
            return solInterface;
        }

        public void setInterface(String solInterface) {
            this.solInterface = solInterface;
        }
        
        public ContractAbi getAbi() {
			return new ContractAbi(abi);
		}
    }

    
    public SoliditySource(String source) {
        this.source = source;
    }

    public SoliditySource(String source, String rawJson) throws JsonParseException, JsonMappingException, IOException {
		this.source = source;
        if(rawJson == null || rawJson.isEmpty()){
        	contractData = new ContractData();
        	contractData.contracts = Collections.emptyMap();
        	contractData.version = "";

           
        } else {
            contractData = new ObjectMapper().readValue(rawJson, ContractData.class);
        }

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

    public static  SoliditySource fromRawJson(String rawJson) throws JsonParseException, JsonMappingException, IOException {
		return new SoliditySource(null,rawJson);
	}
    
    public static  SoliditySource fromRawJson(File rawJson) throws JsonParseException, JsonMappingException, IOException {
		return fromRawJson(new FileInputStream(rawJson));
	}
    
    public static  SoliditySource fromRawJson(InputStream rawJson) throws JsonParseException, JsonMappingException, IOException {
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

	public ContractData getContractData() {
		return contractData;
	}
	
	public boolean isCompiled() {
		return contractData!=null;
	}
}
