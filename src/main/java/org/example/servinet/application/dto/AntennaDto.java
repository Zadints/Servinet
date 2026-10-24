package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.antenna.StatusAntenna;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

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
