package org.example.servinet.bootstrap.compositionroot;

import org.example.servinet.application.port.in.InMemoryDatabaseSetupRepository;
import org.example.servinet.application.service.setup.DatabaseSetupService;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.infrastructure.database.inmemory.setup.InMemoryDatabaseSetup;

public class BootstrapCompositionRoot {

    private final DatabaseSetupService databaseSetupService;
    private final AppExecutor appExecutor;

    public BootstrapCompositionRoot() {
        this.appExecutor = new AppExecutor();
        InMemoryDatabaseSetupRepository memoryRepository = new InMemoryDatabaseSetup();

        this.databaseSetupService = new DatabaseSetupService(memoryRepository, this.appExecutor);

    }

    public DatabaseSetupService getDatabaseSetupService() {
        return databaseSetupService;
    }

    public AppExecutor getAppExecutor() {
        return appExecutor;
    }
}