package org.example.servinet.core.domain.enums;

public enum TechnicalStatus {
    AVAILABLE("DISPONIBLE"),
    BUSY("EN INSTALACIÓN"),
    BREAK("EN DESCANSO");

    private final String value;

    TechnicalStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
