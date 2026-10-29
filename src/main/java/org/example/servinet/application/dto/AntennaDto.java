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
    private LocalDate dateLastMaintenance;
    private LocalTime timeLastMaintenance;
    private byte[] image;
    private StatusAntenna status;
    private Double latitude;
    private Double longitude;

    public AntennaDto(String priority, String name, boolean forReair, boolean forMaintenance, LocalDate dateLastMaintenance, LocalTime timeLastMaintenance, byte[] image, StatusAntenna status, Double latitude, Double longitude) {
        this.priority = priority;
        this.name = name;
        this.forReair = forReair;
        this.forMaintenance = forMaintenance;
        this.dateLastMaintenance = dateLastMaintenance;
        this.timeLastMaintenance = timeLastMaintenance;
        this.image = image;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
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

}
