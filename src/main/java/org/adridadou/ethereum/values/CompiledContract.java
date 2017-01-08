package org.adridadou.ethereum.values;

import org.ethereum.solidity.compiler.CompilationResult;

/**
 * Created by davidroon on 08.01.17.
 */
public class CompiledContract {
    private final ContractAbi abi;
    private final EthData binary;
    private final ContractMetadata metadata;
    private final SoliditySource source;
    private final String name;


    public CompiledContract(ContractAbi abi, EthData binary, ContractMetadata metadata, SoliditySource source, String name) {
        this.abi = abi;
        this.binary = binary;
        this.metadata = metadata;
        this.source = source;
        this.name = name;
    }

    public ContractAbi getAbi() {
        return abi;
    }

    public EthData getBinary() {
        return binary;
    }

    public ContractMetadata getMetadata() {
        return metadata;
    }

    public SoliditySource getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public static CompiledContract from(SoliditySource src, String contractName, CompilationResult.ContractMetadata metadata) {
        return new CompiledContract(
                ContractAbi.of(metadata.abi),
                EthData.of(metadata.bin),
                new ContractMetadata(metadata.metadata),
                src,
                contractName
        );
    }
}
