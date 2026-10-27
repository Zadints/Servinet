package org.example.servinet.application.usecase;

import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.LogEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class GetLogUseCase {
    private final ActivityLogsService activityLogsService;

    public GetLogUseCase(ActivityLogsService activityLogsService) {
        this.activityLogsService = activityLogsService;
    }

    public Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from){
        return activityLogsService.getActivityByDay(userUuid,from);
    }
    public List<LogEntry> getLogs(String userName, int limit){
        return activityLogsService.getLogs(userName,limit);
    }

}
