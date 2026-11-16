package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.domain.entities.LogEntry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLogsRepository implements InMemoryRepository<LogEntry> {

    private Map<String, LogEntry> logs = new ConcurrentHashMap<>();


    @Override
    public List<LogEntry> getAll() {
        return this.logs;
    }


    @Override
    public void setAll(List<LogEntry> logs) {
        this.logs= logs;
    }

    @Override
    public void addEntity(LogEntry s) {
        logs.add(s);
    }

    @Override
    public void remove(String name) {
        logs.removeIf(s -> s.userName().equals(name));
    }

    @Override
    public void edit(LogEntry entity) {
        for (int i = 0; i < logs.size(); i++) {
            if (logs.get(i).userName().equals(entity.userName())) {
                logs.set(i, entity);
                break;
            }
        }
    }


}
