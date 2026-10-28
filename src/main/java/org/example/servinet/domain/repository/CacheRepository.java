package org.example.servinet.domain.repository;

import org.example.servinet.infrastructure.dto.DatabaseConfig;
import org.example.servinet.infrastructure.dto.WhatsAppConfig;

public interface CacheRepository {
    String get(String key);
    DatabaseConfig getDatabaseConfig();
    WhatsAppConfig getWhatsAppConfig();
    String getApiPeruConfig();
}
