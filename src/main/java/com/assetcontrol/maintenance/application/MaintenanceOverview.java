package com.assetcontrol.maintenance.application;

import java.util.List;

public record MaintenanceOverview(
        List<ComputerMaintenanceDue> computers,
        long overdue,
        long upcoming,
        long current
) {
}
