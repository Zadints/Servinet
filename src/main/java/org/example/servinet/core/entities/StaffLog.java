package org.example.servinet.core.entities;

import java.time.LocalDateTime;

public class StaffLog {

    private Staff staffInfo;
    private LocalDateTime createAt;
    private String information;

    public StaffLog(Staff staffInfo, LocalDateTime createAt, String information) {
        this.staffInfo = staffInfo;
        this.createAt = createAt;
        this.information = information;

        //gente Id creamos entidad y apsamos ya para entidades apiladas

    }
}
