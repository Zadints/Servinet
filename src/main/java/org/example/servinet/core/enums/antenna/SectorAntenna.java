package org.example.servinet.core.enums.antenna;

public enum SectorAntenna {
    SECTOR_1("Sector 1"),
    SECTOR_2("Sector 2");

    //agregar más errores
    private final String sector;

    SectorAntenna(String sector) {
        this.sector = sector;
    }

    @Override
    public String toString() {
        return sector;
    }
}
