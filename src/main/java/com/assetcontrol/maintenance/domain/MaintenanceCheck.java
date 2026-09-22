package com.assetcontrol.maintenance.domain;

public enum MaintenanceCheck {
    DONE("Realizado"),
    NOT_APPLICABLE("No aplica");

    private final String label;

    MaintenanceCheck(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
