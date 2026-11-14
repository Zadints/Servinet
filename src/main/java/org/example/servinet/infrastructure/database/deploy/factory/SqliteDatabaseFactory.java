package org.example.servinet.infrastructure.database.deploy.factory;

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

        String url = "jdbc:sqlite:" + databasePath;

        Connection connection = DriverManager.getConnection(url);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }
}
