package com.assetcontrol.assignments.application;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Long id) {
        super("No se encontró la persona con identificador " + id);
    }
}