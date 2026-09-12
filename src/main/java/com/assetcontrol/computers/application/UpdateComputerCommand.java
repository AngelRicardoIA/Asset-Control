package com.assetcontrol.computers.application;

import com.assetcontrol.computers.domain.ComputerType;
import com.assetcontrol.computers.domain.ComputerStatus;

public record UpdateComputerCommand(
        String asset,
        String host,
        ComputerType type,
        String brand,
        String model,
        String serialNumber,
        String chargerSerialNumber,
        String operatingSystem,
        ComputerStatus status,
        Long siteId,
        String newSiteName,
        String observations
) {
}