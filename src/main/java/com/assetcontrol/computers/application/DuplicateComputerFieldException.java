package com.assetcontrol.computers.application;

public class DuplicateComputerFieldException extends RuntimeException {

    private final String field;

    public DuplicateComputerFieldException(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}