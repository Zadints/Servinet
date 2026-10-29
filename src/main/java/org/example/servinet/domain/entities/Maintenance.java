package org.example.servinet.domain.entities;

import org.example.servinet.domain.repository.Identifiable;

import java.time.LocalDateTime;

public class Maintenance implements Identifiable {
    private final String uuid;
    private final String technicaUuid;
    private final String description;
    private final LocalDateTime startMaintenanceDate;
    private final LocalDateTime endMaintenanceDate;
    private byte[] image;

    public Maintenance(String uuid, String technicaUuid, String description, LocalDateTime startMaintenanceDate, LocalDateTime endMaintenanceDate, byte[] image) {
        this.uuid = uuid;
        this.technicaUuid = technicaUuid;
        this.description = description;
        this.startMaintenanceDate = startMaintenanceDate;
        this.endMaintenanceDate = endMaintenanceDate;
        this.image = image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTechnicaUuid() {
        return technicaUuid;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartMaintenanceDate() {
        return startMaintenanceDate;
    }

    public LocalDateTime getEndMaintenanceDate() {
        return endMaintenanceDate;
    }

    public byte[] getImage() {
        return image;
    }
}
