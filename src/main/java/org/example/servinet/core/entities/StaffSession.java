package org.example.servinet.core.entities;

import java.time.LocalDateTime;

public class StaffSession {
    private String idSession;
    private String staffId;
    private LocalDateTime createAt;
    private String machineId;
    private LocalDateTime expiredAt;
    private LocalDateTime lastActivity;
    private String ipAddress;

    public StaffSession(String idSession, String staffId, LocalDateTime createAt, String machineId, LocalDateTime expiredAt, LocalDateTime lastActivity, String ipAddress) {
        this.idSession = idSession;
        this.staffId = staffId;
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
        return staffId;
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
