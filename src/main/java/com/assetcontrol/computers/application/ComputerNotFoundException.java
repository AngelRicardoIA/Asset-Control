package com.assetcontrol.computers.application;

public class ComputerNotFoundException extends RuntimeException {

    public ComputerNotFoundException(Long id) {
        super("No se encontró el equipo con identificador " + id);
    }
}