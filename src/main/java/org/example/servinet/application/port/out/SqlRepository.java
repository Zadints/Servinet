package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.Role;

import java.util.List;

public interface SqlRepository<T> {
    void add(T entity);
    T get(String id);
    InMemoryRepository<T> getAll();
    void delete(String id);
    void update(T entity);
}
