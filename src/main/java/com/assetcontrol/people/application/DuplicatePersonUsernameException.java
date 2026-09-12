package com.assetcontrol.people.application;

public class DuplicatePersonUsernameException extends RuntimeException {

    public DuplicatePersonUsernameException(String username) {
        super("Ya existe una persona con el nombre de usuario " + username);
    }
}