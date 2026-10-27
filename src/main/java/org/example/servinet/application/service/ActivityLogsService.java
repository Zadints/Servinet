package org.example.servinet.application.service;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.LogsRepository;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityLogsService {

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final InMemoryRepository<LogEntry> logEntryInMemoryRepository;
    private final LogsRepository sqlLogModelRepository;
    public ActivityLogsService(InMemoryComplexRepository<Staff> staffInMemoryRepository, InMemoryRepository<LogEntry> logEntryInMemoryRepository, LogsRepository sqlLogModelRepository) {
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.logEntryInMemoryRepository = logEntryInMemoryRepository;

        this.sqlLogModelRepository = sqlLogModelRepository;
    }

    public void addLog(LogType type) {
        addLog(type, "");
    }

    public void addLog(LogType type, String information) {
        try {
            Staff user = staffInMemoryRepository.getCurrent();
            String info = information == null ? "" : information;
            if (info.length() > 200) info = info.substring(0, 200);

            LocalDateTime date = LocalDateTime.now();
            logEntryInMemoryRepository.addEntity(new LogEntry(user.getDisplay(), type.name(), info, date));
            sqlLogModelRepository.insertLog(user.getDisplay(), type.name(), info);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public List<LogEntry> getLogs(String userName, int limit) {
        List<LogEntry> logs =  logEntryInMemoryRepository.getAll();

        List<LogEntry> logsReturn = new ArrayList<>();
        for (LogEntry l: logs){
            if (l.userName().equals(userName)){
                logsReturn.add(l);
            }
        }
        return  logsReturn;
    }

    public Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from) {
        return sqlLogModelRepository.getActivityByDay(userUuid, from);
    }
}