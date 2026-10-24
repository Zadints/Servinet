package org.example.servinet.infrastructure.dto;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String username,
        String password
) {
}
