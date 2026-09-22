package com.assetcontrol.dashboard.application;

public record DashboardAssetStats(long total, long available, long assigned, long loaned, long retired) {
}
