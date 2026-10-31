package org.example.servinet.application.port.out.sql;

import org.example.servinet.domain.enums.antenna.StatusAntenna;

import java.util.List;

public interface ComplexRepository<T>{
    void add(T entity);
    T get(String id);
    List<T> getAll();
    byte[] getImage(String id);
    void delete(String id);
    void update(T entity);
    void updateStatus(String uuid, StatusAntenna status);
    boolean toggleMaintenance(String uuid);
}
