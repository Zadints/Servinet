package org.example.servinet.domain.entities;

import java.time.LocalDateTime;

public class log {

    private User user;
    private LocalDateTime activityAt;
    private String information;

    public log(User user, LocalDateTime createAt, String information) {
        this.user = user;
        this.activityAt = createAt;
        this.information = information;
    }

    public User getStaffInfo() {
        return user;
    }

    public String getInformation() {
        return information;
    }

    public LocalDateTime getCreateAt() {
        return activityAt;
    }
}
