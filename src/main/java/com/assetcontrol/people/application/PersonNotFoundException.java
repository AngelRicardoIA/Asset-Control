package com.assetcontrol.people.application;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Long personId) {
        super("No se encontró la persona con identificador " + personId);
    }
}
