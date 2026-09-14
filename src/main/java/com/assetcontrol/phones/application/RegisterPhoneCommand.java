package com.assetcontrol.phones.application;

public record RegisterPhoneCommand(
        String imei,
        String brand,
        String model,
        Long siteId,
        String newSiteName,
        Long phoneLineId,
        String newLineNumber,
        String newLineCarrier,
        String observations
) {
}