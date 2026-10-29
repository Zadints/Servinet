package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.port.out.InMemoryRepository;

public interface LimitedDataRepository<T, R> {
    void add(T entity);
    T get(String id);
    R getAll();
    void delete(String id);
    void update(T entity);
}
