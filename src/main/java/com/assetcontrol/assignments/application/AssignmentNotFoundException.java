package com.assetcontrol.assignments.application;

public class AssignmentNotFoundException extends RuntimeException {

    public AssignmentNotFoundException(Long id) {
        super("No se encontró la asignación con identificador " + id);
    }
}