package org.example.servinet.domain.entities;

import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.domain.repository.Identifiable;

import javafx.scene.image.Image;
import java.time.LocalDateTime;
import java.util.List;

public class Antenna implements Identifiable {
    private final String uuid;
    private int priority;
    private String name;
    private List<Maintenance> maintenance;
    private boolean forReair;
    private boolean forMaintenance;
    private final LocalDateTime dateCreate;
    private LocalDateTime dateLastMaintenance;
    private int countDaysOn;
    private int countDaysOff;

    private byte[] image;
    private StatusAntenna status;

    private Double latitude;
    private Double longitude;

    public Antenna(String uuid, int priority, String name, List<Maintenance> maintenance, boolean forReair, boolean forMaintenance, LocalDateTime dateCreate, LocalDateTime dateLastMaintenance, int countDaysOn, int countDaysOff, byte[] image, StatusAntenna status, Double latitude, Double longitude) {
        this.uuid = uuid;
        this.priority = priority;
        this.name = name;
        this.maintenance = maintenance;
        this.forReair = forReair;
        this.forMaintenance = forMaintenance;
        this.dateCreate = dateCreate;
        this.dateLastMaintenance = dateLastMaintenance;
        this.countDaysOn = countDaysOn;
        this.countDaysOff = countDaysOff;
        this.image = image;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public int getPriority() {
        return priority;
    }

    public List<Maintenance> getMaintenance() {
        return maintenance;
    }

    public boolean isForReair() {
        return forReair;
    }

    public LocalDateTime getDateLastMaintenance() {
        return dateLastMaintenance;
    }

    public int getCountDaysOn() {
        return countDaysOn;
    }

    public int getCountDaysOff() {
        return countDaysOff;
    }

    public byte[] getImage() {
        return image;
    }

    public StatusAntenna getStatus() {
        return status;
    }

    public LocalDateTime getDateCreate() {
        return dateCreate;
    }

    public boolean isForMaintenance() {
        return forMaintenance;
    }

    public String getName() {
        return name;
    }


    public void addMaintenance(Maintenance mainte) {
        this.maintenance.add(mainte);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setForMaintenance(boolean forMaintenance) {
        this.forMaintenance = forMaintenance;
    }

    public void setForReair(boolean forReair) {
        this.forReair = forReair;
    }

    public void setDateLastMaintenance(LocalDateTime dateLastMaintenance) {
        this.dateLastMaintenance = dateLastMaintenance;
    }

    public void setCountDaysOff(int countDaysOff) {
        this.countDaysOff = countDaysOff;
    }

    public void setCountDaysOn(int countDaysOn) {
        this.countDaysOn = countDaysOn;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setStatus(StatusAntenna status) {
        this.status = status;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }



}
