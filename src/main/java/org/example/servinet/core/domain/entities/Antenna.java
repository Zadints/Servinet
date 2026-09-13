package org.example.servinet.core.domain.entities;

import org.example.servinet.core.domain.enums.antenna.SectorAntenna;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;
import org.example.servinet.core.domain.repository.Identifiable;

import javafx.scene.image.Image;
import java.time.LocalDateTime;
import java.util.List;

public class Antenna implements Identifiable {
    private final String uuid;
    private short priority;
    private String name;
    private List<Maintenance> maintenance;
    private boolean forReair;
    private boolean forMaintenance;
    private final LocalDateTime dateCreate;
    private LocalDateTime dateLastMaintenance;
    private int countDaysOn;
    private int countDaysOff;

    private Image image;
    private StatusAntenna status;

    public Antenna(String uuid, short priority, String name,  boolean forReair, boolean forMaintenance, LocalDateTime dateLastMaintenance, int countDaysOn, int countDaysOff, Image image, StatusAntenna status, LocalDateTime dateCreate) {
        this.uuid = uuid;
        this.priority = priority;
        this.name = name;
        this.forReair = forReair;
        this.forMaintenance = forMaintenance;
        this.dateLastMaintenance = dateLastMaintenance;
        this.countDaysOn = countDaysOn;
        this.countDaysOff = countDaysOff;
        this.image = image;
        this.status = status;
        this.dateCreate = dateCreate;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public short getPriority() {
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

    public Image getImage() {
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

    public void setImage(Image image) {
        this.image = image;
    }

    public void setStatus(StatusAntenna status) {
        this.status = status;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }
}
