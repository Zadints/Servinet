package org.example.servinet.infrastructure.database.inmemory;

import com.sun.jdi.InvalidTypeException;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.domain.entities.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLogsRepository implements InMemoryRepository<LogEntry> {

    private List<LogEntry> logs = new ArrayList<>();


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
