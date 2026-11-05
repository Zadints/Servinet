package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.domain.entities.InstallationReport;
import org.example.servinet.domain.entities.technicians.TechnicalLogs;
import org.example.servinet.infrastructure.dto.TechnicalStatic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TechnicalRepository {
    List<OrderContainer> getTechnicianLogs(String staffUuid);
    List<TechnicalStatic> getTechnicianStatistics();
    List<OrderContainer> getTechnicianLogsByClient(String clientDni);
    Optional<TechnicalLogs> getActiveInstallation(String staffUuid);
    void acceptInstallation(
            String staffUuid,
            String orderId,
            String clientDni
    );

    void finishInstallation(
            String staffUuid,
            InstallationReport report
    );

    boolean isInstallationInProgress(String orderId);

    void rejectInstallation(String staffUuid, String orderId);
}
