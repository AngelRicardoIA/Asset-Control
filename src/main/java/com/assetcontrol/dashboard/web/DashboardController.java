package com.assetcontrol.dashboard.web;

import com.assetcontrol.dashboard.application.DashboardService;
import com.assetcontrol.maintenance.application.MaintenanceOverviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final MaintenanceOverviewService maintenanceOverview;

    public DashboardController(DashboardService dashboardService, MaintenanceOverviewService maintenanceOverview) {
        this.dashboardService = dashboardService;
        this.maintenanceOverview = maintenanceOverview;
    }

    @GetMapping("/")
    public String showDashboard(Model model) {
        model.addAttribute("overview", dashboardService.overview());
        model.addAttribute("maintenance", maintenanceOverview.overview());
        return "dashboard/index";
    }
}
