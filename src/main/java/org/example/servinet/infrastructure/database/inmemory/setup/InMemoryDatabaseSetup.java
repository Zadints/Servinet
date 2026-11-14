package org.example.servinet.infrastructure.database.inmemory.setup;

import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.port.in.InMemoryDatabaseSetupRepository;
import org.example.servinet.domain.exception.DatabaseException;

public class InMemoryDatabaseSetup implements InMemoryDatabaseSetupRepository {
    private DatabaseSetup databaseSetup;
    @Override
    public void setDatabaseSetup(DatabaseSetup databaseSetup){
        this.databaseSetup = databaseSetup;
    }
    @Override
    public DatabaseSetup getDatabaseSetup() {
        if (databaseSetup == null){
            System.out.println("No estableciste conexion aún");
            throw new DatabaseException("No estableciste conexion aún");
        }
        return databaseSetup;
    }



}
