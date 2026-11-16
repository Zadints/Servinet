package org.example.servinet.application.port.out.sql;

public interface SqlLimitedDataRepository<T, R> {
    void add(T entity);
    T get(String id);
    R getAll();
    void delete(String id);
    void update(T entity);
}
