package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.antenna.StatusAntenna;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

public class AntennaDto {
    private int priority;
    private String name;
    private boolean forReair;
    private boolean forMaintenance;
    private LocalDate dateLastMaintenance;
    private LocalTime timeLastMaintenance;
    private byte[] image;
    private final int countDaysOn;
    private final int countDaysOff;
    private StatusAntenna status;
    private Double latitude;
    private Double longitude;

    public AntennaDto(int priority, String name, boolean forReair, boolean forMaintenance, LocalDate dateLastMaintenance, LocalTime timeLastMaintenance, byte[] image, int countDaysOn, int countDaysOff, StatusAntenna status, Double latitude, Double longitude) {
        this.priority = priority;
        this.name = name;
        this.forReair = forReair;
        this.forMaintenance = forMaintenance;
        this.dateLastMaintenance = dateLastMaintenance;
        this.timeLastMaintenance = timeLastMaintenance;
        this.image = image;
        this.countDaysOn = countDaysOn;
        this.countDaysOff = countDaysOff;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public boolean isForReair() {
        return forReair;
    }

    public boolean isForMaintenance() {
        return forMaintenance;
    }

    public LocalDate getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public LocalTime getTimeLastMaintenance() {
        return timeLastMaintenance;
    }

    public byte[] getImage() {
        return image;
    }

    public StatusAntenna getStatus() {
        return status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public int getCountDaysOn() {
        return countDaysOn;
    }

    public int getCountDaysOff() {
        return countDaysOff;
    }
}
