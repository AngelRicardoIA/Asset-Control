package com.assetcontrol.phones.application;

import com.assetcontrol.phones.domain.PhoneStatus;

public record UpdatePhoneCommand(
        String imei,
        String brand,
        String model,
        Long siteId,
        String newSiteName,
        Long phoneLineId,
        String newLineNumber,
        String newLineCarrier,
        String observations,
        PhoneStatus status
) {
}