package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.domain.entities.LogEntry;

import java.time.LocalDate;
import java.util.Map;

public interface SqlLogsRepository {
    void insertLog(String userName, String type, String information);

    InMemoryRepository<LogEntry> getLogs(int limit);

    Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from);
}
