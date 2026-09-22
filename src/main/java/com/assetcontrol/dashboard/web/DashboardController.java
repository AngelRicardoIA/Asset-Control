package com.assetcontrol.dashboard.web;

import com.assetcontrol.dashboard.application.DashboardService;
import com.assetcontrol.accessories.application.AccessoryService;
import com.assetcontrol.maintenance.application.MaintenanceOverviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final MaintenanceOverviewService maintenanceOverview;
    private final AccessoryService accessoryService;

    public DashboardController(DashboardService dashboardService, MaintenanceOverviewService maintenanceOverview,
                               AccessoryService accessoryService) {
        this.dashboardService = dashboardService;
        this.maintenanceOverview = maintenanceOverview;
        this.accessoryService = accessoryService;
    }

    @GetMapping("/")
    public String showDashboard(Model model) {
        model.addAttribute("overview", dashboardService.overview());
        model.addAttribute("maintenance", maintenanceOverview.overview());
        model.addAttribute("accessories", accessoryService.inventory("", null, false));
        return "dashboard/index";
    }
}
