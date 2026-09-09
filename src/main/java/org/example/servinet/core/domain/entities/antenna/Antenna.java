package org.example.servinet.core.domain.entities.antenna;

import org.example.servinet.core.domain.enums.antenna.SectorAntenna;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;
import org.example.servinet.core.domain.repository.Identifiable;

import java.time.LocalDateTime;

public class Antenna implements Identifiable {
    private final String uuid;
    private short priority;
    private String name;
    private SectorAntenna sector;
    private String image;
    private StatusAntenna status;
    private int maintenanceCount;
    private boolean forReair; ///contexto: ¿para reparar?
    private final LocalDateTime dateCreate;
    private LocalDateTime dateLastMaintenance;
    private int daysOn;

    public Antenna(String uuid, short priority, String name, SectorAntenna sector, String image, StatusAntenna status, int maintenanceCount, boolean forReair, LocalDateTime dateLastMaintenance, LocalDateTime dateCreate, int daysOn) {
        this.uuid = uuid;
        this.priority = priority;
        this.name = name;
        this.sector = sector;
        this.image = image;
        this.status = status;
        this.maintenanceCount = maintenanceCount;
        this.forReair = forReair;
        this.dateLastMaintenance = dateLastMaintenance;
        this.dateCreate = dateCreate;
        this.daysOn = daysOn;
    }

    public String getUuid() {
        return uuid;
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

    public String getImage() {
        return image;
    }

    public StatusAntenna getStatus() {
        return status;
    }

    public int getMaintenanceCount() {
        return maintenanceCount;
    }

    public boolean isForReair() {
        return forReair;
    }

    public LocalDateTime getDateCreate() {
        return dateCreate;
    }

    public LocalDateTime getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public int getDaysOn() {
        return daysOn;
    }
}
