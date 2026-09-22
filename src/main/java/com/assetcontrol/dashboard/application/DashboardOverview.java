package com.assetcontrol.dashboard.application;

import java.util.List;

public record DashboardOverview(
        DashboardAssetStats computers,
        DashboardAssetStats phones,
        long people,
        long overdueLoans,
        List<DashboardOverdueLoan> recentOverdueLoans
) {
}
