package org.example.servinet.application.dto.database;

import org.example.servinet.application.enums.AuthenticationMode;
import org.example.servinet.application.enums.DeploymentMode;

public record DatabaseSetup(
        DeploymentMode mode,
        String host,
        Integer port,
        String database,
        String username,
        String password) {
}
