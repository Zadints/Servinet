package org.example.mineide.core.enums;

public enum ServerVersion {
    VANILLA_1_7_10("Vanilla 1.7.10"),
    VANILLA_1_8_9("Vanilla 1.8.9"),
    VANILLA_1_9_4("Vanilla 1.9.4"),
    VANILLA_1_10_2("Vanilla 1.10.2"),
    VANILLA_1_11_2("Vanilla 1.11.2"),
    VANILLA_1_12_2("Vanilla 1.12.2"),
    VANILLA_1_13_2("Vanilla 1.13.2"),
    VANILLA_1_14_4("Vanilla 1.14.4"),
    VANILLA_1_15_2("Vanilla 1.15.2"),
    VANILLA_1_16_5("Vanilla 1.16.5"),
    VANILLA_1_17_1("Vanilla 1.17.1"),
    VANILLA_1_18_2("Vanilla 1.18.2"),
    VANILLA_1_19_4("Vanilla 1.19.4"),
    VANILLA_1_20_2("Vanilla 1.20.2"),
    VANILLA_1_20_4("Vanilla 1.20.4"),
    VANILLA_1_20_6("Vanilla 1.20.6"),
    VANILLA_1_21("Vanilla 1.21"),
    VANILLA_1_21_1("Vanilla 1.21.1"),
    VANILLA_1_21_6("Vanilla 1.21.6"),
    VANILLA_1_21_4("Vanilla 1.20.4"),
    VANILLA_1_21_9("Vanilla 1.21.9"),
    VANILLA_1_21_11("Vanilla 1.21.11"),
    VANILLA_26_1("Vanilla 26.1"),
    VANILLA_26_1_2("Vanilla 26.1.2"),;

    //agregar más versiones de los otros sofwares

    private final String displayName;

    ServerVersion(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
