package org.example.servinet.core.entities.antenna;

import org.example.servinet.core.enums.antenna.SectorAntenna;
import org.example.servinet.core.enums.antenna.StatusAntenna;

import java.time.LocalDateTime;

public class Antenna {
    private String id;
    private short priority;
    private String name;
    private SectorAntenna sector;
    private String image;
    private StatusAntenna status;
    private int maintenanceCount;
    private boolean forReair; ///contexto: ¿para reparar?
    private LocalDateTime dateCreate;
    private LocalDateTime dateLastMaintenance;
    private int daysOn;

    public Antenna(String id, short priority, String name, SectorAntenna sector, String image, StatusAntenna status, int maintenanceCount, boolean forReair, LocalDateTime dateLastMaintenance, LocalDateTime dateCreate, int daysOn) {
        this.id = id;
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

    public String getId() {
        return id;
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
