package org.example.servinet.core.domain.entities;

import org.example.servinet.core.domain.enums.PayMethods;

import java.time.LocalDateTime;

public class Pay {

    private final String id;
    private final String clientDni;
    private final Short mont;
    private final Boolean paid;
    private final LocalDateTime payDate;
    private final PayMethods payMethod;
    private final String obs;

    public Pay(
            String id,
            String clientDni,
            Short mont,
            Boolean paid,
            LocalDateTime payDate,
            PayMethods payMethod,
            String obs
    ) {
        this.id = id;
        this.clientDni = clientDni;
        this.mont = mont;
        this.paid = paid;
        this.payDate = payDate;
        this.payMethod = payMethod;
        this.obs = obs;
    }

    public String getId() {
        return id;
    }

    public String getClientDni() {
        return clientDni;
    }

    public Short getMont() {
        return mont;
    }

    public Boolean getPaid() {
        return paid;
    }

    public LocalDateTime getPayDate() {
        return payDate;
    }

    public PayMethods getPayMethod() {
        return payMethod;
    }

    public String getObs() {
        return obs;
    }
}
