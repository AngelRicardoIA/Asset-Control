package com.assetcontrol.phones.maintenance.web;

import com.assetcontrol.phones.application.PhoneService;
import com.assetcontrol.phones.maintenance.application.PhoneMaintenanceService;
import com.assetcontrol.phones.maintenance.domain.PhoneMaintenanceType;
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
@RequestMapping("/phones/{phoneId}/maintenance")
public class PhoneMaintenanceController {

    private final PhoneMaintenanceService maintenanceService;
    private final PhoneService phoneService;

    public PhoneMaintenanceController(
            PhoneMaintenanceService maintenanceService,
            PhoneService phoneService
    ) {
        this.maintenanceService = maintenanceService;
        this.phoneService = phoneService;
    }

    @GetMapping("/new")
    public String showNewMaintenanceForm(
            @PathVariable Long phoneId,
            Model model
    ) {
        prepareFormModel(phoneId, model);
        model.addAttribute("maintenance", new PhoneMaintenanceRecordForm());

        return "phones/maintenance/new";
    }

    @PostMapping
    public String createMaintenanceRecord(
            @PathVariable Long phoneId,
            @Valid @ModelAttribute("maintenance") PhoneMaintenanceRecordForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(phoneId, model);
            return "phones/maintenance/new";
        }

        maintenanceService.create(form.toCommand(phoneId));

        return "redirect:/phones/" + phoneId;
    }

    private void prepareFormModel(Long phoneId, Model model) {
        model.addAttribute("phone", phoneService.findById(phoneId));
        model.addAttribute("maintenanceTypes", PhoneMaintenanceType.values());
    }
}
