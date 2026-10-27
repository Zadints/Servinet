package org.example.servinet.application.port.out;

import com.sun.jdi.InvalidTypeException;
import org.example.servinet.domain.entities.LogEntry;

import java.util.List;

public interface InMemoryRepository<T> {
    T getCurrent();
    List<T> getAll();

    void setCurrent(T entity);
    void setAll(List<T> listEntity);
    void addEntity(T entity);

    void editCurrent(T entity) throws IllegalArgumentException;
    void removeList(String id);
    void resetCurrent() throws InvalidTypeException;
}
