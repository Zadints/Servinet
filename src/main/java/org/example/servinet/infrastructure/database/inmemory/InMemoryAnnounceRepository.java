package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.domain.entities.Announce;
import org.example.servinet.domain.entities.Announcement;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAnnounceRepository implements InMemoryRepository<Announcement> {

    private static List<Announcement> announcesList = new ArrayList<>();

    @Override
    public List<Announcement> getAll() {
        return announcesList;
    }
    @Override
    public void setAll(List<Announcement> listEntity) {
        announcesList = listEntity;
    }

    @Override
    public void addEntity(Announcement entity) {
        announcesList.add(entity);
    }

    @Override
    public void remove(String id) {
        announcesList.removeIf(s -> s.getUuid().equals(id));
    }

}
