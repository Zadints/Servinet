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
    private boolean forReair; ///¿para reparar?
    private LocalDateTime dateCreate;
    private LocalDateTime dateLastMaintenance;
    private int daysOn;
}
