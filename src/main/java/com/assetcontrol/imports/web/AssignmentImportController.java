package com.assetcontrol.imports.web;

import com.assetcontrol.imports.application.AssignmentCsvImportService;
import com.assetcontrol.imports.application.AssignmentImportPreview;
import com.assetcontrol.imports.application.ComputerInventoryCsvImportService;
import com.assetcontrol.imports.application.ComputerInventoryImportPreview;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/assignment-imports")
public class AssignmentImportController {

    private static final String COMPUTER_PREVIEW_KEY = "computerInventoryImportPreview";
    private static final String PHONE_PREVIEW_KEY = "phoneAssignmentImportPreview";

    private final ComputerInventoryCsvImportService computerImportService;
    private final AssignmentCsvImportService phoneImportService;

    public AssignmentImportController(
            ComputerInventoryCsvImportService computerImportService,
            AssignmentCsvImportService phoneImportService
    ) {
        this.computerImportService = computerImportService;
        this.phoneImportService = phoneImportService;
    }

    @GetMapping("/computers")
    public String showComputerForm(Model model) {
        return showComputerImportForm(model);
    }

    @GetMapping("/phones")
    public String showPhoneForm(Model model) {
        return showPhoneImportForm(model);
    }

    @PostMapping("/computers/preview")
    public String previewComputers(
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            Model model
    ) {
        try {
            ComputerInventoryImportPreview preview = computerImportService.preview(file);
            session.setAttribute(COMPUTER_PREVIEW_KEY, preview);
            model.addAttribute("preview", preview);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return showComputerImportForm(model);
    }

    @PostMapping("/phones/preview")
    public String previewPhones(
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            Model model
    ) {
        try {
            AssignmentImportPreview preview = phoneImportService.previewPhones(file);
            session.setAttribute(PHONE_PREVIEW_KEY, preview);
            model.addAttribute("preview", preview);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return showPhoneImportForm(model);
    }

    @PostMapping("/computers/confirm")
    public String confirmComputers(
            HttpSession session,
            RedirectAttributes attributes
    ) {
        ComputerInventoryImportPreview preview = (ComputerInventoryImportPreview) session
                .getAttribute(COMPUTER_PREVIEW_KEY);

        if (preview == null) {
            attributes.addFlashAttribute("errorMessage", "Primero selecciona y valida un CSV.");
            return "redirect:/computers";
        }

        computerImportService.importPreview(preview);
        session.removeAttribute(COMPUTER_PREVIEW_KEY);
        attributes.addFlashAttribute(
                "successMessage",
                preview.rows().size() + " equipos y asignaciones importados correctamente."
        );
        return "redirect:/computers";
    }

    @PostMapping("/phones/confirm")
    public String confirmPhones(
            HttpSession session,
            RedirectAttributes attributes
    ) {
        AssignmentImportPreview preview = (AssignmentImportPreview) session
                .getAttribute(PHONE_PREVIEW_KEY);

        if (preview == null) {
            attributes.addFlashAttribute("errorMessage", "Primero selecciona y valida un CSV.");
            return "redirect:/phones";
        }

        phoneImportService.importPreview(preview);
        session.removeAttribute(PHONE_PREVIEW_KEY);
        attributes.addFlashAttribute(
                "successMessage",
                preview.rows().size() + " asignaciones importadas correctamente."
        );
        return "redirect:/phones";
    }

    private String showComputerImportForm(Model model) {
        model.addAttribute("kind", AssignmentImportPreview.Kind.COMPUTER);
        model.addAttribute(
                "headers",
                String.join(", ", ComputerInventoryCsvImportService.HEADERS)
        );
        return "assignment-imports/new";
    }

    private String showPhoneImportForm(Model model) {
        model.addAttribute("kind", AssignmentImportPreview.Kind.PHONE);
        model.addAttribute(
                "headers",
                "imei,username,assignment_type,assigned_at,due_date,returned_at,notes"
        );
        return "assignment-imports/new";
    }
}
