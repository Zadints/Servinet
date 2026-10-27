package org.example.servinet.application.port.out;

import com.sun.jdi.InvalidTypeException;

import java.util.List;

public interface InMemoryComplexRepository<T> {
    T getCurrent();
    List<T> getAll();

    void setCurrent(T entity);
    void setAll(List<T> listEntity);
    void addEntity(T entity);

    void editCurrent(T entity) throws IllegalArgumentException;
    void removeList(String id);
    void resetCurrent() throws InvalidTypeException;
}
