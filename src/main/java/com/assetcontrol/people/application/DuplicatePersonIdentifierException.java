package com.assetcontrol.people.application;

public class DuplicatePersonIdentifierException extends RuntimeException {

    public DuplicatePersonIdentifierException(String externalId) {
        super("Ya existe una persona con el ID " + externalId);
    }
}