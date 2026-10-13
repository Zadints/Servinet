package org.example.servinet.core.domain.entities.technicians;

import org.example.servinet.core.domain.entities.Staff;
import org.example.servinet.core.domain.enums.TechnicianStatus;

import java.util.List;

public class Technician {

    private Staff technician;
    private TechnicianStatus status;

    private Order instNow;
    private List<Order> instEnded;
    private List<Order> instCancelled;
    private List<Order> allInstLog;

    public Technician(Staff technician, TechnicianStatus status) {
        this.technician = technician;
        this.status = status;
    }
}
