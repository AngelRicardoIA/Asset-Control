package com.assetcontrol.computers.web;

import com.assetcontrol.computers.application.ComputerService;
import com.assetcontrol.computers.application.DuplicateComputerFieldException;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.computers.domain.ComputerType;
import com.assetcontrol.sites.application.SiteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.assetcontrol.assignments.application.ComputerAssignmentService;

@Controller
@RequestMapping("/computers")
public class ComputerController {

    private final ComputerService computerService;
    private final SiteService siteService;
    private final ComputerAssignmentService assignmentService;

    public ComputerController(
            ComputerService computerService,
            SiteService siteService,
            ComputerAssignmentService assignmentService
    ) {
        this.computerService = computerService;
        this.siteService = siteService;
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public String showComputerList(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) ComputerStatus status,
            @RequestParam(required = false) ComputerType type,
            Model model
    ) {
        model.addAttribute("computers", computerService.search(query, status, type));
        model.addAttribute("query", query);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("computerStatuses", ComputerStatus.values());
        model.addAttribute("computerTypes", ComputerType.values());
        return "computers/index";
    }

    @GetMapping("/{id}")
    public String showComputerDetail(@PathVariable Long id, Model model) {
        model.addAttribute("computer", computerService.findById(id));
        model.addAttribute("assignments", assignmentService.findHistoryByComputerId(id));
        return "computers/detail";
    }

    @GetMapping("/new")
    public String showNewComputerForm(Model model) {
        addFormData(model);
        model.addAttribute("computer", new ComputerForm());
        return "computers/new";
    }

    @PostMapping
    public String createComputer(
            @Valid @ModelAttribute("computer") ComputerForm computerForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            addFormData(model);
            return "computers/new";
        }

        try {
            computerService.create(computerForm.toCommand());
            return "redirect:/computers";
        } catch (DuplicateComputerFieldException exception) {
            String message = exception.getField().equals("asset")
                    ? "Ya existe un equipo con este asset."
                    : "Ya existe un equipo con este hostname.";

            bindingResult.rejectValue(
                    exception.getField(),
                    "computer." + exception.getField() + ".duplicate",
                    message
            );

            addFormData(model);
            return "computers/new";
        }
    }

    private void addFormData(Model model) {
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute("computerStatuses", ComputerStatus.values());
        model.addAttribute("computerTypes", ComputerType.values());
    }
}