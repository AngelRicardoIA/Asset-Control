package com.assetcontrol.maintenance.application;

import com.assetcontrol.computers.domain.Computer;

import java.time.LocalDate;

public record ComputerMaintenanceDue(
        Computer computer,
        LocalDate lastPreventiveAt,
        LocalDate nextDueAt,
        MaintenanceDueStatus status
) {
}
