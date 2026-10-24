package org.example.servinet.domain.entities.technicians;

import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.TechnicalStatus;

import java.util.List;

public class Technical {

    private Staff Technical;
    private TechnicalStatus status;

    private Order instNow;
    private List<Order> instEnded;
    private List<Order> instCancelled;
    private List<Order> allInstLog;

    public Technical(Staff technician, TechnicalStatus status) {
        this.Technical = technician;
        this.status = status;
    }

    public Staff getTechnical() {
        return Technical;
    }

    public TechnicalStatus getStatus() {
        return status;
    }

    public Order getInstNow() {
        return instNow;
    }

    public List<Order> getInstEnded() {
        return instEnded;
    }

    public List<Order> getInstCancelled() {
        return instCancelled;
    }

    public List<Order> getAllInstLog() {
        return allInstLog;
    }
}
