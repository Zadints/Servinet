package org.example.servinet.core.application.dto;

import javafx.scene.image.Image;
import org.example.servinet.core.domain.entities.Maintenance;
import org.example.servinet.core.domain.enums.antenna.SectorAntenna;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class AntennaDto {
    private short priority;
    private String name;
    private boolean forReair;
    private boolean forMaintenance;
    private int countDaysOn;
    private LocalDateTime dateLastMaintenance;
    private Path image;
    private StatusAntenna status;

    public AntennaDto(short priority, String name, boolean forReair, int countDaysOn, StatusAntenna status, Path image, LocalDateTime dateLastMaintenance, boolean forMaintenance) {
        this.priority = priority;
        this.name = name;
        this.forReair = forReair;
        this.countDaysOn = countDaysOn;
        this.status = status;
        this.image = image;
        this.dateLastMaintenance = dateLastMaintenance;
        this.forMaintenance = forMaintenance;
    }

    public short getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public boolean isForReair() {
        return forReair;
    }

    public Path getImage() {
        return image;
    }

    public StatusAntenna getStatus() {
        return status;
    }

    public LocalDateTime getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public int getCountDaysOn() {
        return countDaysOn;
    }

    public boolean isForMaintenance() {
        return forMaintenance;
    }
}
