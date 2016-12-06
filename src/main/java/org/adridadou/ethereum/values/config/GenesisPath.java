package org.adridadou.ethereum.values.config;

/**
 * Created by davidroon on 06.12.16.
 * This code is released under Apache 2 license
 */
public class GenesisPath {
    public final String path;

    public GenesisPath(String path) {
        this.path = path;
    }

    public static GenesisPath path(final String path) {
        return new GenesisPath(path);
    }
}
