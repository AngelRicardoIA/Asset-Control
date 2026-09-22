package com.assetcontrol.maintenance.application;

public enum MaintenanceDueStatus {
    CURRENT("Al día"),
    UPCOMING("Por vencer"),
    OVERDUE("Vencido");

    private final String label;

    MaintenanceDueStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
