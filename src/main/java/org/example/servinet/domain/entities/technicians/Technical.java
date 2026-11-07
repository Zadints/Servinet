package org.example.servinet.domain.entities.technicians;

import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.TechnicalStatus;

import java.util.List;

public class Technical {
    private final Staff technical;
    private int installedCount;

    public Technical(Staff technical, int installedCount) {
        this.technical = technical;
        this.installedCount = installedCount;
    }

    public Staff getTechnical() {
        return technical;
    }
    public String getTechnicalUuid() {
        return technical.getUuid();
    }

    public int getInstalledCount() {
        return installedCount;
    }

    public void setInstalledCount(int installedCount) {
        this.installedCount = installedCount;
    }
}
