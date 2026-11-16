package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.DeploymentMode;
import org.example.servinet.infrastructure.dto.DatabaseConfig;

import java.util.Optional;

public interface SqlCacheManagerRepository {
    Optional<DeploymentMode> getDeployModeType();
    Optional<DatabaseConfig> getDatabaseConfig();
    void updateDatabaseConfig(DatabaseSetup db);
}
