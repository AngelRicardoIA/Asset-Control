package com.assetcontrol.dashboard.web;

import com.assetcontrol.dashboard.application.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String showDashboard(Model model) {
        model.addAttribute("overview", dashboardService.overview());
        return "dashboard/index";
    }
}
