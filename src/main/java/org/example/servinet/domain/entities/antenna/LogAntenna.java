package org.example.servinet.domain.entities.antenna;

import org.example.servinet.domain.entities.User;

import java.time.LocalDateTime;

public class LogAntenna {
    private String id;
    private String imageMaintenance;
    private String description;
    private LocalDateTime dateTime;
    private User employee;

    public LogAntenna(String id, String imageMaintenance, String description, LocalDateTime dateTime, User employee) {


        this.id = id;
        this.imageMaintenance = imageMaintenance;
        this.description = description;
        this.dateTime = dateTime;
        this.employee = employee;
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
