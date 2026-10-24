package org.example.servinet.domain.enums;

public enum OrderStatus {
    PENDING("PENDIENTE"),
    INSTALLING("INSTALANDO"),
    INSTALLED_ACTIVE("INSTALACION_ACTIVA");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Estado de orden desconocido: " + value
        );
    }

}
