package com.assetcontrol.accessories.domain;

public enum AccessoryType {
    MONITOR("Monitor"), KEYBOARD("Teclado"), MOUSE("Mouse"), HEADSET("Diadema"), OTHER("Otro");
    private final String label;
    AccessoryType(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String getPluralLabel() { return switch (this) {
        case MONITOR -> "Monitores";
        case KEYBOARD -> "Teclados";
        case MOUSE -> "Mouse";
        case HEADSET -> "Diademas";
        case OTHER -> "Otros";
    }; }
}
