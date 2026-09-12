package com.assetcontrol.sites.application;

public class SiteNotFoundException extends RuntimeException {

    public SiteNotFoundException() {
        super("The selected site does not exist or is inactive.");
    }
}