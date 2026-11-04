package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.domain.entities.technicians.TechnicalLogs;
import org.example.servinet.infrastructure.dto.TechnicalStatic;

import java.util.List;
import java.util.UUID;

public interface TechnicalRepository {
    List<OrderContainer> getTechnicianLogs(String staffUuid);
    List<TechnicalStatic> getTechnicianStatistics();
}
