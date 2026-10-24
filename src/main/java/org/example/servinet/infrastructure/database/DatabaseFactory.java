package org.example.servinet.infrastructure.database;

import org.example.servinet.infrastructure.dto.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseFactory {

    private final DatabaseConfig config;

    public DatabaseFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        String url = String.format(
                "jdbc:sqlserver://%s:%d;"
                        + "databaseName=%s;"
                        + "encrypt=true;"
                        + "trustServerCertificate=true",
                config.host(),
                config.port(),
                config.database()
        );

        return DriverManager.getConnection(
                url,
                config.username(),
                config.password()
        );
    }
}