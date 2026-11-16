package org.example.servinet.infrastructure.database.deploy.scripts;

import java.io.IOException;
import java.sql.SQLException;

public interface DatabaseScriptExecutor {
    void executeScript(String resourcePath) throws IOException, SQLException;
}
