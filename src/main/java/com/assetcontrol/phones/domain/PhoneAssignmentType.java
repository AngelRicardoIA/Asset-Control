package com.assetcontrol.phones.domain;

public enum PhoneAssignmentType {
    ASSIGNMENT("Asignación"),
    LOAN("Préstamo");

    private final String label;

    PhoneAssignmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}