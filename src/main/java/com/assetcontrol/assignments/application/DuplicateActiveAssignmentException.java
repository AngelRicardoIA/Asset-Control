package com.assetcontrol.assignments.application;

public class DuplicateActiveAssignmentException extends RuntimeException {

    public DuplicateActiveAssignmentException() {
        super("La persona ya tiene una asignación activa para este equipo.");
    }
}