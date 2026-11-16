package org.example.servinet.application.port.out.inmemory;

import org.example.servinet.application.dto.database.DatabaseSetup;

public interface InMemoryDatabaseSetupRepository {
    void setDatabaseSetup(DatabaseSetup databaseSetup);
    DatabaseSetup getDatabaseSetup();
}
