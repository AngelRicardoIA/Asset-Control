package com.assetcontrol.computers.application;

import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.computers.domain.ComputerType;

public record CreateComputerCommand(
        String asset,
        String host,
        ComputerType type,
        String brand,
        String model,
        String serialNumber,
        String operatingSystem,
        ComputerStatus status,
        Long siteId,
        String newSiteName,
        String observations
) {
}