package com.assetcontrol.maintenance.application;

import com.assetcontrol.maintenance.domain.ComputerMaintenanceType;

import java.time.LocalDate;

public record CreateComputerMaintenanceRecordCommand(
        Long computerId,
        ComputerMaintenanceType recordType,
        LocalDate performedAt,
        String description,
        String performedBy
) {
}