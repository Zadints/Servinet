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

        System.out.println("DB Host: " + config.host());
        System.out.println("DB Port: " + config.port());
        System.out.println("DB Name: " + config.database());
        System.out.println("DB User: " + config.username());
        System.out.println("DB Password configurada: "
                + (config.password() != null && !config.password().isBlank()));

        return DriverManager.getConnection(
                url,
                config.username(),
                config.password()
        );
    }
}