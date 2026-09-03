package org.example.servinet.domain.entities;

import java.time.LocalDateTime;

public class UserSession {
    private String idSession;
    private String userId;
    private LocalDateTime createAt;
    private String machineId;
    private LocalDateTime expiredAt;
    private LocalDateTime lastActivity;
    private String ipAddress;

    public UserSession(String idSession, String userId, LocalDateTime createAt, String machineId, LocalDateTime expiredAt, LocalDateTime lastActivity, String ipAddress) {
        this.idSession = idSession;
        this.userId = userId;
        this.createAt = createAt;
        this.machineId = machineId;
        this.expiredAt = expiredAt;
        this.lastActivity = lastActivity;
        this.ipAddress = ipAddress;
    }

    public String getIdSession() {
        return idSession;
    }

    public String getStaffId() {
        return userId;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public String getMachineId() {
        return machineId;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
