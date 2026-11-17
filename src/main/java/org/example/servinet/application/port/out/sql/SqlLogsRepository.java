package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.enums.LogType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SqlLogsRepository {
    void insertLog(String userName, String type, String information);

    Map<LogType, List<LogEntry>> getLogs(int limit);
    Map<LogType, List<LogEntry>> getLogs(String userName,int limit);

    Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from);
}
