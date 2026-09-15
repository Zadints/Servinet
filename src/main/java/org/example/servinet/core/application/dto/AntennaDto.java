package org.example.servinet.core.application.dto;

import javafx.scene.image.Image;
import org.example.servinet.core.domain.entities.Maintenance;
import org.example.servinet.core.domain.enums.antenna.SectorAntenna;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AntennaDto {
    private String priority;
    private String name;
    private boolean forReair;
    private boolean forMaintenance;
    private String countDaysOn;
    private LocalDate dateLastMaintenance;
    private LocalTime timeLastMaintenance;
    private Path image;
    private StatusAntenna status;

    public AntennaDto(
            String priority,
            String name,
            boolean forReair,
            boolean forMaintenance,
            LocalDate dateLastMaintenance,
            LocalTime timeLastMaintenance,
            Path image,
            StatusAntenna status,
            String countDaysOn
    ) {
        this.priority = priority;
        this.name = name;
        this.forReair = forReair;
        this.forMaintenance = forMaintenance;
        this.dateLastMaintenance = dateLastMaintenance;
        this.timeLastMaintenance = timeLastMaintenance;
        this.image = image;
        this.status = status;
        this.countDaysOn = countDaysOn;
    }

    public String getPriority() {
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

    public LocalDate getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public LocalTime getTimeLastMaintenance() {
        return timeLastMaintenance;
    }

    public String getCountDaysOn() {
        return countDaysOn;
    }

    public boolean isForMaintenance() {
        return forMaintenance;
    }
}
