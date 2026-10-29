package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.technicians.Technical;

import java.util.List;

public interface InMemoryTechnicalRepository {

    List<Technical> getAll();
    void setAll(List<Technical> listEntity);
    void addTechnical(Technical entity);

    void remove(String id);

    void edit(Technical entity);

    void setTechnicalAlreadyExist(int value);
    void setTechnicalAvailable(int value);
    void setTechnicalBusy(int value);

    int getTechnicalAlreadyExist();
    int getTechnicalAvailable();
    int getTechnicalBusy();
}
