package org.example.servinet.core.domain.enums;

public enum PayMethods {

    EFECTIVO("Efectivo"),
    YAPE("Yape"),
    PLIN("Plin"),
    TRANSFERENCIA("Transferencia");

    private final String value;

    PayMethods(String value) {
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
