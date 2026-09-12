package com.assetcontrol.sites.application;

public class SiteRequiredException extends RuntimeException {

    public SiteRequiredException() {
        super("Selecciona una ubicación o escribe una nueva.");
    }
}