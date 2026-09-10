package org.example.servinet.core.application.dto;

import org.example.servinet.core.domain.enums.antenna.SectorAntenna;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class AntennaDto {
    private short priority;
    private String name;
    private SectorAntenna sector;
    private Path image;
    private StatusAntenna status;
    private boolean forReair;
    private boolean forMaintenance;
    private LocalDateTime lastMaintenanceDate;
    private int countDaysOn;

    public AntennaDto(short priority, String name, SectorAntenna sector, Path image, boolean forReair, boolean forMaintenance, LocalDateTime lastMaintenanceDate, int countDaysOn, StatusAntenna status) {
        this.priority = priority;
        this.name = name;
        this.sector = sector;
        this.image = image;
        this.forReair = forReair;
        this.forMaintenance = forMaintenance;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.countDaysOn = countDaysOn;
        this.status = status;
    }

    public short getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public StatusAntenna getStatus() {
        return status;
    }

    public boolean isForReair() {
        return forReair;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public int getCountDaysOn() {
        return countDaysOn;
    }

    public boolean isForMaintenance() {
        return forMaintenance;
    }

    public Path getImage() {
        return image;
    }

    public SectorAntenna getSector() {
        return sector;
    }
}
