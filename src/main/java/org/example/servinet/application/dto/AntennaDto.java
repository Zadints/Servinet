package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.antenna.SectorAntenna;
import org.example.servinet.domain.enums.antenna.StatusAntenna;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class AntennaDto {
    private short priority;
    private String name;
    private SectorAntenna sector;
    private Path image;
    private StatusAntenna status;
    private int maintenanceCount;
    private boolean forReair; ///contexto: ¿para reparar?
    private LocalDateTime dateLastMaintenance;
    private int daysOn;

    public AntennaDto(short priority, String name, SectorAntenna sector, Path image, StatusAntenna status, int maintenanceCount, LocalDateTime dateLastMaintenance, int daysOn, boolean forReair) {
        this.priority = priority;
        this.name = name;
        this.sector = sector;
        this.image = image;
        this.status = status;
        this.maintenanceCount = maintenanceCount;
        this.dateLastMaintenance = dateLastMaintenance;
        this.daysOn = daysOn;
        this.forReair = forReair;
    }

    public short getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public SectorAntenna getSector() {
        return sector;
    }

    public Path getImage() {
        return image;
    }

    public int getMaintenanceCount() {
        return maintenanceCount;
    }

    public StatusAntenna getStatus() {
        return status;
    }

    public boolean isForReair() {
        return forReair;
    }

    public LocalDateTime getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public int getDaysOn() {
        return daysOn;
    }
}
