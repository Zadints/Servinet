package org.example.servinet.application.port.out;

import java.util.List;

public interface SqlComplexRepository<T> {
    void add(T entity);
    T get(String id);
    byte[] getImage(String id);
    List<T> getAll();
    void delete(String id);
    void update(T entity);
}
