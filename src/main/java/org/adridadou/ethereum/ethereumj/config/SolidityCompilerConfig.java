package org.adridadou.ethereum.ethereumj.config;

import java.nio.file.Path;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 *
 * # Solidity options
 * solc {
 * # Full path to solc executable
 * # If path is not provided, bundled Solidity Compiler is used
 * path=null
 * }
 */
public class SolidityCompilerConfig {
    private final Path path;

    public SolidityCompilerConfig(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
