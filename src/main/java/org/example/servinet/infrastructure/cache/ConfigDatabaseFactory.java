package org.example.servinet.infrastructure.cache;

public class ConfigDatabaseFactory {
    private static final Path DB_PATH = Path.of(
            System.getProperty("user.home"),
            "AppData",
            "Local",
            "Servinet",
            "config.db"
    );
}
