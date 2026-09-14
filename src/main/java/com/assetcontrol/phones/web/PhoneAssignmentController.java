package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.PhoneAssignmentService;
import com.assetcontrol.phones.application.PhoneService;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class PhoneAssignmentController {

    private final PhoneService phoneService;
    private final PhoneAssignmentService phoneAssignmentService;

    public PhoneAssignmentController(
            PhoneService phoneService,
            PhoneAssignmentService phoneAssignmentService
    ) {
        this.phoneService = phoneService;
        this.phoneAssignmentService = phoneAssignmentService;
    }

    @GetMapping("/phones/{phoneId}/assignments/new")
    public String showNewAssignmentForm(
            @PathVariable Long phoneId,
            Model model
    ) {
        model.addAttribute("phone", phoneService.findById(phoneId));
        model.addAttribute("phoneId", phoneId);
        model.addAttribute("form", new PhoneAssignmentForm());
        addFormOptions(model);
        return "phones/assignments/new";
    }

    @PostMapping("/phones/{phoneId}/assignments")
    public String assignPhone(
            @PathVariable Long phoneId,
            @Valid @ModelAttribute("form") PhoneAssignmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("phone", phoneService.findById(phoneId));
            model.addAttribute("phoneId", phoneId);
            addFormOptions(model);
            return "phones/assignments/new";
        }

        try {
            phoneAssignmentService.assign(phoneId, form.toCommand());
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("assignment", exception.getMessage());
            model.addAttribute("phone", phoneService.findById(phoneId));
            model.addAttribute("phoneId", phoneId);
            addFormOptions(model);
            return "phones/assignments/new";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Teléfono asignado correctamente."
        );

        return "redirect:/phones/" + phoneId;
    }

    @PostMapping("/phone-assignments/{assignmentId}/return")
    public String returnPhone(
            @PathVariable Long assignmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate returnedAt,
            RedirectAttributes redirectAttributes
    ) {
        try {
            phoneAssignmentService.returnAssignment(assignmentId, returnedAt);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Asignación devuelta correctamente."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/phones";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("people", phoneAssignmentService.findPeople());
        model.addAttribute("assignmentTypes", PhoneAssignmentType.values());
    }
}