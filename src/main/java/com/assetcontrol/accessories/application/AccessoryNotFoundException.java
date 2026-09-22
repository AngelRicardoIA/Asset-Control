package com.assetcontrol.accessories.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccessoryNotFoundException extends RuntimeException {
    public AccessoryNotFoundException(Long id) {
        super("No se encontró el accesorio " + id + ".");
    }
}
