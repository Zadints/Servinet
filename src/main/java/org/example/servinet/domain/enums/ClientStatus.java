package org.example.servinet.domain.enums;

public enum ClientStatus {
    EN_INSTALACION("En instalación"),
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido"),
    EN_DESINSTALACION("En desinstalación"),
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