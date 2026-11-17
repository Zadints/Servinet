package org.example.servinet.application.service;

import org.example.servinet.application.port.out.inmemory.InMemoryComplexRepository;
import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.application.port.out.sql.SqlLogsRepository;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ActivityLogsService {

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    //private final InMemoryRepository<LogEntry> logEntryInMemoryRepository;
    private final SqlLogsRepository sqlLogModelRepository;
    public ActivityLogsService(
            InMemoryComplexRepository<Staff> staffInMemoryRepository,
            SqlLogsRepository sqlLogModelRepository
            ) {
        this.staffInMemoryRepository = staffInMemoryRepository;
        //this.logEntryInMemoryRepository = logEntryInMemoryRepository;

        this.sqlLogModelRepository = sqlLogModelRepository;
    }

    public void addLog(LogType type, String information) {
        try {
            Staff user = staffInMemoryRepository.getCurrent();
            String info = information == null ? "" : information;
            if (info.length() > 200) info = info.substring(0, 200);

            LocalDateTime date = LocalDateTime.now();

            String autor = user.getRolName() + " • " + user.getDisplay();

            sqlLogModelRepository.insertLog(user.getDisplay(), type.name(), info);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    /**
     * Obtiene la cantidad requerida de logs sin importar tipo ni usuario.
     **/
    public Map<LogType, List<LogEntry>> getLogs(int limit) {
        return sqlLogModelRepository.getLogs(limit);
    }

    public Map<LogType, List<LogEntry>> getLogs(String staffName, int limit) {
        return sqlLogModelRepository.getLogs(staffName, limit);
    }

    public Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from) {
        return sqlLogModelRepository.getActivityByDay(userUuid, from);
    }
}