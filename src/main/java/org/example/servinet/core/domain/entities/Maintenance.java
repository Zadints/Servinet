package org.example.servinet.core.domain.entities;

import java.time.LocalDateTime;

public class Maintenance {
    private final String id;
    private final String description;
    private final String uuidUser;
    private final LocalDateTime startMaintenanceDate;
    private final LocalDateTime endMaintenanceDate;

    public Maintenance(String id, String description, String uuidUser, LocalDateTime startMaintenanceDate, LocalDateTime endMaintenanceDate) {
        this.id = id;
        this.description = description;
        this.uuidUser = uuidUser;
        this.startMaintenanceDate = startMaintenanceDate;
        this.endMaintenanceDate = endMaintenanceDate;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getEndMaintenanceDate() {
        return endMaintenanceDate;
    }

    public LocalDateTime getStartMaintenanceDate() {
        return startMaintenanceDate;
    }

    public String getUuidUser() {
        return uuidUser;
    }

    public String getDescription() {
        return description;
    }
}
