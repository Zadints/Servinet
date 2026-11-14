package org.example.servinet.application.service.setup;

import javafx.concurrent.Task;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.port.in.InMemoryDatabaseSetupRepository;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.infrastructure.dto.DatabaseConfig;

import java.nio.file.Path;

public class DatabaseSetupService {
    private final InMemoryDatabaseSetupRepository inMemoryDatabaseSetup;
    private final AppExecutor appExecutor;

    public DatabaseSetupService(InMemoryDatabaseSetupRepository inMemoryDatabaseSetup, AppExecutor appExecutor) {
        this.inMemoryDatabaseSetup = inMemoryDatabaseSetup;
        this.appExecutor = appExecutor;
    }

    public Path saveSqliteDatabase(DatabaseSetup dto){
        Task<Path> task = new Task() {
            @Override
            protected Path call() throws Exception {
                return null;
            }
        }
    }

    public DatabaseConfig saveSqlServerDatabase(DatabaseSetup dto){

    }


}
