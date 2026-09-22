package com.assetcontrol.maintenance.application;

import com.assetcontrol.maintenance.domain.MaintenanceCheck;

import java.time.LocalDate;

public record CreatePreventiveMaintenanceCommand(
        Long computerId,
        LocalDate performedAt,
        String performedBy,
        String description,
        MaintenanceCheck updatesCheck,
        MaintenanceCheck driversCheck,
        MaintenanceCheck externalCleaning,
        MaintenanceCheck internalCleaning,
        String checklistNotes
) {
}
