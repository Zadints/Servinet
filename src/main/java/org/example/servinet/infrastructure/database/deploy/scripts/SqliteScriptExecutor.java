package org.example.servinet.infrastructure.database.deploy.scripts;

import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteScriptExecutor implements DatabaseScriptExecutor {

    private final DatabaseFactory databaseFactory;

    public SqliteScriptExecutor(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void executeScript(String sqlResource) throws IOException, SQLException{
        try (Connection connection = databaseFactory.getConnection()) {
            sqliteScriptExecute(connection, sqlResource);


        }
    }

    private void sqliteScriptExecute (
            Connection connection, String sqlResource) throws IOException, SQLException {

        String script;

        try (InputStream input = getClass()
                .getResourceAsStream(sqlResource)) {

            if (input == null) {
                throw new IOException(
                        "No existe el script SQL: " + sqlResource
                );
            }

            script = new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }

        boolean oldAutoCommit = connection.getAutoCommit();

        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {

            for (String sql : script.split(";")) {

                String command = sql.trim();

                if (!command.isEmpty()) {
                    statement.execute(command);
                }
            }

            connection.commit();

        } catch (SQLException e) {

            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }
}
