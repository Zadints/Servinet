package org.example.servinet.infrastructure.database.deploy.test;

import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcConnectionTester implements DatabaseConnectionTester {

    private final DatabaseFactory databaseFactory;

    public JdbcConnectionTester(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public boolean testConnection() {

        try (Connection connection = databaseFactory.getConnection()) {
            return connection.isValid(5);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al comprobar la conexión", e
            );
        }
    }
}