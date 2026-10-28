package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.InMemoryTechnicalRepository;
import org.example.servinet.domain.entities.technicians.Technical;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCacheTechnicalRepository implements InMemoryTechnicalRepository {

    private List<Technical> technicians;
    private int technicalAlreadyExist;
    private int technicalAvailable;
    private int technicalBusy;

    @Override
    public List<Technical> getAll() {
        return technicians;
    }

    @Override
    public void setAll(List<Technical> listEntity) {
        technicians = listEntity;
    }

    @Override
    public void addTechnical(Technical entity) {
        technicians.add(entity);
    }

    @Override
    public void remove(String id) {
        technicians.removeIf(s -> s.getTechnical().getUuid().equals(id));
    }

    @Override
    public void edit(Technical entity) {
        for (int i = 0; i < technicians.size(); i++) {
            if (technicians.get(i).getTechnical().getUuid().equals(entity.getTechnical().getUuid())) {
                technicians.set(i, entity);
                break;
            }
        }
    }

    @Override
    public void setTechnicalAlreadyExist(int value) {
        technicalAlreadyExist = value;
    }

    @Override
    public void setTechnicalAvailable(int value) {
        technicalAvailable = value;
    }

    @Override
    public void setTechnicalBusy(int value) {
        technicalBusy = value;
    }

    @Override
    public int getTechnicalAlreadyExist() {
        return technicalAlreadyExist;
    }

    @Override
    public int getTechnicalAvailable() {
        return technicalAvailable;
    }

    @Override
    public int getTechnicalBusy() {
        return technicalBusy;
    }
}
