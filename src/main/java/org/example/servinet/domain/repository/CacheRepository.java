package org.example.servinet.domain.repository;

import org.example.servinet.infrastructure.dto.DatabaseConfig;

public interface CacheRepository {
    String get(String key);
    DatabaseConfig getDatabaseConfig();
}
