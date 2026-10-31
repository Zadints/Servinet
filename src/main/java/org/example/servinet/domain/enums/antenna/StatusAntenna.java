package org.example.servinet.domain.enums.antenna;

public enum StatusAntenna {
    ACTIVE("Activa"),
    OFFLINE("Desconectada"),
    DEGRADED("Degradada"),
    MAINTENANCE("En mantenimiento");

    private final String description;

    StatusAntenna(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
