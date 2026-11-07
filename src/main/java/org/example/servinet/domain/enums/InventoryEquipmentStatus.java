package org.example.servinet.domain.enums;

public enum InventoryEquipmentStatus {

    AVAILABLE("Disponible"),
    ASSIGNED("Asignado a técnico"),
    INSTALLED("Instalado en cliente"),
    MAINTENANCE("En mantenimiento"),
    DAMAGED("Dañado"),
    LOST("Extraviado"),
    RETIRED("Dado de baja");

    private final String displayName;

    InventoryEquipmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}