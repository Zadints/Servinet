package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.client.ClientPlan;

import java.util.List;

public interface InMemoryRepository<T> {

    List<T> getAll();
    void setAll(List<T> listEntity);

    void addEntity(T entity);

    void remove(String id);

    void edit(T entity);


}
