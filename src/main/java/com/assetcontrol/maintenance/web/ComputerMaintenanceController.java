package com.assetcontrol.maintenance.web;

import com.assetcontrol.computers.application.ComputerService;
import com.assetcontrol.maintenance.application.ComputerMaintenanceService;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceType;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/computers/{computerId}/maintenance")
public class ComputerMaintenanceController {

    private final ComputerMaintenanceService maintenanceService;
    private final ComputerService computerService;

    public ComputerMaintenanceController(
            ComputerMaintenanceService maintenanceService,
            ComputerService computerService
    ) {
        this.maintenanceService = maintenanceService;
        this.computerService = computerService;
    }

    @GetMapping("/new")
    public String showNewMaintenanceForm(
            @PathVariable Long computerId,
            Model model
    ) {
        prepareFormModel(computerId, model);
        model.addAttribute("maintenance", new ComputerMaintenanceRecordForm());

        return "maintenance/new";
    }

    @PostMapping
    public String createMaintenanceRecord(
            @PathVariable Long computerId,
            @Valid @ModelAttribute("maintenance") ComputerMaintenanceRecordForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(computerId, model);
            return "maintenance/new";
        }

        maintenanceService.create(form.toCommand(computerId));

        return "redirect:/computers/" + computerId;
    }

    private void prepareFormModel(Long computerId, Model model) {
        model.addAttribute("computer", computerService.findById(computerId));
        model.addAttribute(
                "maintenanceTypes",
                ComputerMaintenanceType.values()
        );
    }
}