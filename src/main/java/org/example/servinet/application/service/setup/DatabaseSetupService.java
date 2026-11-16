package org.example.servinet.application.service.setup;

import javafx.concurrent.Task;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.port.in.InMemoryDatabaseSetupRepository;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.test.DatabaseConnectionTester;
import org.example.servinet.infrastructure.dto.DatabaseConfig;

import java.nio.file.Path;

public class DatabaseSetupService {
    private final InMemoryDatabaseSetupRepository inMemoryDatabaseSetup;
    private final AppExecutor appExecutor;

    public DatabaseSetupService(InMemoryDatabaseSetupRepository inMemoryDatabaseSetup, AppExecutor appExecutor) {
        this.inMemoryDatabaseSetup = inMemoryDatabaseSetup;
        this.appExecutor = appExecutor;
    }

    public boolean isValidConnection(DatabaseConnectionTester dbTester) throws DatabaseException {
        return dbTester.testConnection();
        //aqui colocar log de test
    }

    public DatabaseConfig toDatabaseConfig(DatabaseSetup dto){
        if (dto.host() == null || dto.host().isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar el servidor o dirección IP"
            );
        }

        if (dto.port() == null || dto.port() < 1 || dto.port() > 65535) {
            throw new IllegalArgumentException(
                    "El puerto debe estar entre 1 y 65535"
            );
        }

        if (dto.database() == null || dto.database().isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar el nombre de la base de datos"
            );
        }

        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar el usuario de SQL Server"
            );
        }

        if (dto.password() == null || dto.password().isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar la contraseña de SQL Server"
            );
        }
        return new DatabaseConfig(dto.host(), dto.port(), dto.database(), dto.username(), dto.password());
    }


}
