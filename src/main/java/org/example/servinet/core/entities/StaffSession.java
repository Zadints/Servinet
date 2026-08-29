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
}
