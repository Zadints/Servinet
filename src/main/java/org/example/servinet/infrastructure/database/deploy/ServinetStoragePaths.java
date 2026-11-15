package org.example.servinet.infrastructure.database.deploy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServinetStoragePaths {

    private final Path root;
    private final Path cache;
    private final Path database;
    private static final String DB_USER = "user-database.db";
    private static final String DB_CACHE = "cache-database.db";

    public ServinetStoragePaths(){
        String appData = System.getenv("LOCALAPPDATA");

        Path base = appData != null && !appData.isBlank()
                ? Path.of(appData)
                : Path.of(System.getProperty("user.home"));

        root = base.resolve("Servinet");
        cache = root.resolve("cache");
        database = root.resolve("database");
    }


    public void createDirectories() throws IOException {
        Files.createDirectories(cache);
        Files.createDirectories(database);
    }

    public Path getRoot() {
        return root;
    }

    public Path getCache() {
        return cache;
    }

    public Path getDatabaseUser() {
        return database;
    }

    public Path getCacheDatabaseFile() {
        return cache.resolve(DB_CACHE);
    }

    public Path getUserDatabaseFile() {
        return database.resolve(DB_USER);
    }

    public boolean cacheDatabaseExists() {
        return Files.isRegularFile(getCacheDatabaseFile());
    }

    public boolean userDatabaseExists() {
        return Files.isRegularFile(getUserDatabaseFile());
    }
}
