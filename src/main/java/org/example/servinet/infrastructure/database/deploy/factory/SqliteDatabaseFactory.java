package org.example.servinet.infrastructure.database.deploy.factory;

import com.fasterxml.jackson.core.io.IOContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteDatabaseFactory implements DatabaseFactory {

    private final Path databasePath;

    public SqliteDatabaseFactory(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath().normalize();
    }

    @Override
    public Connection getConnection() throws SQLException {

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        );

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }
}
