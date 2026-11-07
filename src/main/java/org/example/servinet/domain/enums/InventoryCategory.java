package org.example.servinet.domain.enums;

public enum InventoryCategory {

    // EQUIPOS DE INTERNET
    ROUTER("Routers", true),
    MODEM("Módems", true),
    ONT("ONT / ONU", true),
    ACCESS_POINT("Puntos de acceso", true),
    WIFI_REPEATER("Repetidores WiFi", true),
    CPE("Equipos CPE", true),
    NETWORK_ANTENNA("Antenas de red", true),
    SWITCH("Switches", true),
    POE_INJECTOR("Inyectores PoE", true),
    POE_SPLITTER("Divisores PoE", true),

    // CABLEADO
    UTP_CABLE("Cable UTP", false),
    FTP_CABLE("Cable FTP", false),
    FIBER_OPTIC_CABLE("Fibra óptica", false),
    DROP_CABLE("Cable drop", false),
    COAXIAL_CABLE("Cable coaxial", false),
    PATCH_CORD("Patch cords", true),
    POWER_CABLE("Cables de alimentación", true),

    // CONECTORES
    RJ45_CONNECTOR("Conectores RJ45", false),
    RJ11_CONNECTOR("Conectores RJ11", false),
    FIBER_CONNECTOR("Conectores de fibra", false),
    FIBER_ADAPTER("Adaptadores de fibra", true),
    FIBER_PIGTAIL("Pigtails", false),
    FIBER_SPLITTER("Splitters ópticos", true),
    FIBER_COUPLER("Acopladores de fibra", true),
    KEYSTONE("Keystones", true),
    NETWORK_ROSETTE("Rosetas de red", true),

    // ALIMENTACIÓN ELÉCTRICA
    ROUTER_POWER_SUPPLY("Cargadores de router", true),
    POWER_ADAPTER("Adaptadores de corriente", true),
    POWER_STRIP("Regletas eléctricas", true),
    POE_POWER_SUPPLY("Fuentes de alimentación PoE", true),

    // INSTALACIÓN Y MONTAJE
    FIBER_BOX("Cajas de fibra", true),
    DISTRIBUTION_BOX("Cajas de distribución", true),
    CABLE_DUCT("Canaletas", false),
    CONDUIT("Tubos conduit", false),
    CABLE_TIE("Bridas", false),
    CABLE_CLIP("Grapas para cable", false),
    INSULATION_TAPE("Cinta aislante", false),
    SCREWS("Tornillos", false),
    WALL_PLUGS("Tarugos", false),
    MOUNTING_BRACKET("Soportes de montaje", true),
    POLE_CLAMP("Abrazaderas para poste", true),
    CABLE_ANCHOR("Anclajes para cable", false),

    // OTROS MATERIALES
    OTHER("Otros materiales", false);

    private final String displayName;
    private final boolean recoverable;

    InventoryCategory(String displayName, boolean recoverable) {
        this.displayName = displayName;
        this.recoverable = recoverable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    @Override
    public String toString() {
        return displayName;
    }
}