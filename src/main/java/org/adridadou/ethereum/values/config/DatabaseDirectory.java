package org.adridadou.ethereum.values.config;

/**
 * Created by davidroon on 06.12.16.
 * This code is released under Apache 2 license
 */
public class DatabaseDirectory {
    public final String directory;

    public DatabaseDirectory(String directory) {
        this.directory = directory;
    }

    public static DatabaseDirectory db(final String directory) {
        return new DatabaseDirectory(directory);
    }
}
