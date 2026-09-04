package org.example.servinet.domain.entities.antenna;

import org.example.servinet.domain.entities.User;
import org.example.servinet.domain.repository.Identifiable;

import java.time.LocalDateTime;

public class LogAntenna implements Identifiable {
    private String Uuid;
    private String imageMaintenance;
    private String description;
    private LocalDateTime dateTime;
    private User employee;

    public LogAntenna(String Uuid, String imageMaintenance, String description, LocalDateTime dateTime, User employee) {


        this.Uuid = Uuid;
        this.imageMaintenance = imageMaintenance;
        this.description = description;
        this.dateTime = dateTime;
        this.employee = employee;
    }

    @Override
    public String getUuid() {
        return Uuid;
    }

    public String getImageMaintenance() {
        return imageMaintenance;
    }

    public User getEmployee() {
        return employee;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
