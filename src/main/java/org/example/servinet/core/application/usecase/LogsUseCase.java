package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.entities.LogEntry;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.infrastructure.database.models.LogModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LogsUseCase {

    public static void addLog(LogType type) {
        addLog(type, "");
    }

    public static void addLog(LogType type, String information) {
        try {
            User user = SessionUseCase.getActualSessionUser();
            String info = information == null ? "" : information;
            if (info.length() > 200) info = info.substring(0, 200);

            LogModel.insertLog(user.getUuid(), user.getName(), type.name(), info);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static List<LogEntry> getLogs(String userUuid, int limit) {
        return LogModel.getLogs(userUuid, limit);
    }

    public static Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from) {
        return LogModel.getActivityByDay(userUuid, from);
    }
}