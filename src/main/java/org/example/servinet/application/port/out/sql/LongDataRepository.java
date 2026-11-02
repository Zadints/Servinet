package org.example.servinet.application.port.out.sql;

import java.math.BigDecimal;

public interface LongDataRepository<T, R, C> {
    void add(T entity);
    C get(String id);
    R getPage(String uuid, int offset, int pageSize);
    void delete(String id);
    void update(T entity);
    BigDecimal getAllMoney();
    C getAll();
}
