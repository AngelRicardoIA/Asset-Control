package com.assetcontrol.shared.web;

public enum InventorySortDirection {
    ASCENDING("Ascendente"),
    DESCENDING("Descendente");

    private final String label;

    InventorySortDirection(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
