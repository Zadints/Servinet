package org.example.servinet.core.domain.enums;

public enum ClientStatus {
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido"),
    RETIRADO("Retirado");

    private final String value;

    ClientStatus(String value) {
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