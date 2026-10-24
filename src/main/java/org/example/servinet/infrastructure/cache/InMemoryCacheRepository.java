package org.example.servinet.infrastructure.cache;

import org.example.servinet.domain.repository.CacheRepository;
import org.example.servinet.infrastructure.dto.DatabaseConfig;

import java.util.Map;

public class InMemoryCacheRepository implements CacheRepository {

    private final Map<String, String> translations;

    public InMemoryCacheRepository(Map<String, String> translations) {
        this.translations = Map.copyOf(translations);
    }

    @Override
    public String get(String key) {
        return translations.getOrDefault(key, key);
    }

    @Override
    public DatabaseConfig getDatabaseConfig() {
        return new DatabaseConfig(
                get("database.host"),
                Integer.parseInt(get("database.port")),
                get("database.name"),
                get("database.username"),
                get("repository.get")
        );
    }
}
