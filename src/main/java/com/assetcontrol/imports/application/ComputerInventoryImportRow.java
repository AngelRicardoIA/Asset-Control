package com.assetcontrol.imports.application;

import java.time.LocalDate;

public record ComputerInventoryImportRow(
        String siteName,
        String employeeNumber,
        String fullName,
        String jobTitle,
        String department,
        String managerName,
        String username,
        String email,
        String computerType,
        String host,
        String brand,
        String model,
        String asset,
        String serialNumber,
        String operatingSystem,
        String chargerSerialNumber,
        LocalDate assignedAt
) {
}
