package com.assetcontrol.dashboard.application;

import java.time.LocalDate;

public record DashboardOverdueLoan(
        boolean computer,
        Long assetId,
        String identity,
        String username,
        LocalDate dueDate
) {
}
