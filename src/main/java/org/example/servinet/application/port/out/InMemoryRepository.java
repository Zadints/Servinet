package org.example.servinet.application.port.out;

import java.util.List;

public interface InMemoryRepository<T> {

    List<T> getAll();
    void setAll(List<T> listEntity);

    void addEntity(T entity);

    void remove(String id);
}
