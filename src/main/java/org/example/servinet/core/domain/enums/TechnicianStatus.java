package org.example.servinet.core.domain.enums;

public enum TechnicianStatus {
    AVAILABLE("DISPONIBLE"),
    BUSY("EN INSTALACIÓN"),
    BREAK("EN DESCANSO");

    private final String value;

    TechnicianStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
