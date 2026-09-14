package com.assetcontrol.phones.application;

public class PhoneNotFoundException extends RuntimeException {

    public PhoneNotFoundException(Long phoneId) {
        super("No existe el teléfono con id " + phoneId + ".");
    }
}