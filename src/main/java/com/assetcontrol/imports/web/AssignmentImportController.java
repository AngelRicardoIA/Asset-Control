package com.assetcontrol.imports.web;

import com.assetcontrol.imports.application.AssignmentCsvImportService;
import com.assetcontrol.imports.application.AssignmentImportPreview;
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

    private static final String PREVIEW_KEY = "assignmentImportPreview";
    private final AssignmentCsvImportService importService;

    public AssignmentImportController(AssignmentCsvImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/computers")
    public String showComputerForm(Model model) {
        return showForm(model, AssignmentImportPreview.Kind.COMPUTER);
    }

    @GetMapping("/phones")
    public String showPhoneForm(Model model) {
        return showForm(model, AssignmentImportPreview.Kind.PHONE);
    }

    @PostMapping("/computers/preview")
    public String previewComputers(@RequestParam("file") MultipartFile file, HttpSession session, Model model) {
        return preview(file, session, model, AssignmentImportPreview.Kind.COMPUTER);
    }

    @PostMapping("/phones/preview")
    public String previewPhones(@RequestParam("file") MultipartFile file, HttpSession session, Model model) {
        return preview(file, session, model, AssignmentImportPreview.Kind.PHONE);
    }

    @PostMapping("/confirm")
    public String confirm(HttpSession session, RedirectAttributes attributes) {
        AssignmentImportPreview preview = (AssignmentImportPreview) session.getAttribute(PREVIEW_KEY);
        if (preview == null) {
            attributes.addFlashAttribute("errorMessage", "Primero selecciona y valida un CSV.");
            return "redirect:/computers";
        }
        importService.importPreview(preview);
        session.removeAttribute(PREVIEW_KEY);
        attributes.addFlashAttribute("successMessage", preview.rows().size() + " asignaciones importadas correctamente.");
        return "redirect:" + (preview.kind() == AssignmentImportPreview.Kind.COMPUTER ? "/computers" : "/phones");
    }

    private String preview(MultipartFile file, HttpSession session, Model model, AssignmentImportPreview.Kind kind) {
        try {
            AssignmentImportPreview preview = kind == AssignmentImportPreview.Kind.COMPUTER ? importService.previewComputers(file) : importService.previewPhones(file);
            session.setAttribute(PREVIEW_KEY, preview);
            model.addAttribute("preview", preview);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return showForm(model, kind);
    }

    private String showForm(Model model, AssignmentImportPreview.Kind kind) {
        model.addAttribute("kind", kind);
        model.addAttribute("headers", kind == AssignmentImportPreview.Kind.COMPUTER
                ? "host,username,assignment_type,assigned_at,due_date,returned_at,notes"
                : "imei,username,assignment_type,assigned_at,due_date,returned_at,notes");
        return "assignment-imports/new";
    }
}
