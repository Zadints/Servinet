package org.example.servinet.infrastructure.database.deploy.scripts;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class SqlServerScriptExecutor implements DatabaseScriptExecutor {

    private static final Pattern GO_SEPARATOR = Pattern.compile("(?im)^\\s*GO\\s*$");
    private final DatabaseFactory databaseFactory;

    public SqlServerScriptExecutor(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void executeScript(String resourcePath)
            throws IOException, SQLException {

        String script = loadScript(resourcePath);

        try (Connection connection = databaseFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {

                String[] batches = GO_SEPARATOR.split(script);

                for (String batch : batches) {

                    String command = batch.trim();

                    if (command.isBlank()) continue;

                    statement.execute(command);
                }

                connection.commit();

            } catch (SQLException e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    private String loadScript(String resourcePath) throws IOException {

        try (InputStream input = getClass()
                .getResourceAsStream(resourcePath)) {

            if (input == null) {
                throw new IOException(
                        "No se encontró el archivo SQL: " + resourcePath
                );
            }

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}
