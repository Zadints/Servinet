package org.example.servinet.application.dto.database;

public record AuthLogin(
        String passwordHash,
        String userUuid
) {
}
