package org.example.servinet.core.domain.enums;

public enum PlanStatusClient {
    AL_DIA("Al día"),
    CON_DEUDA("Con deuda");

    private final String value;

    PlanStatusClient(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}