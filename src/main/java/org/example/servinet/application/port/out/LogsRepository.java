package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.LogEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LogsRepository {
    void insertLog(
            String userName,
            String type,
            String information);
    InMemoryRepository<LogEntry> getLogs(
            String userUuid,
            int limit);
    Map<LocalDate, Integer> getActivityByDay(
            String userUuid,
            LocalDate from);
}
