package com.assetcontrol.shared.web;

public enum InventorySort {
    ALPHABETICAL("Alfabético"),
    CHRONOLOGICAL("Cronológico");

    private final String label;

    InventorySort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
