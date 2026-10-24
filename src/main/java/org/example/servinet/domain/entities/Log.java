package org.example.servinet.domain.entities;

import java.time.LocalDateTime;

public class Log {

    private Staff user;
    private LocalDateTime activityAt;
    private String information;

    public Log(Staff user, LocalDateTime createAt, String information) {
        this.user = user;
        this.activityAt = createAt;
        this.information = information;
    }

    public Staff getStaffInfo() {
        return user;
    }

    public String getInformation() {
        return information;
    }

    public LocalDateTime getCreateAt() {
        return activityAt;
    }
}
