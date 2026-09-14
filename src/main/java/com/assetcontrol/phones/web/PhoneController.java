package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.PhoneAssignmentService;
import com.assetcontrol.phones.application.PhoneService;
import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneStatus;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/phones")
public class PhoneController {

    private final PhoneService phoneService;
    private final PhoneAssignmentService phoneAssignmentService;
    private final com.assetcontrol.sites.application.SiteService siteService;

    public PhoneController(
            PhoneService phoneService,
            PhoneAssignmentService phoneAssignmentService,
            com.assetcontrol.sites.application.SiteService siteService
    ) {
        this.phoneService = phoneService;
        this.phoneAssignmentService = phoneAssignmentService;
        this.siteService = siteService;
    }

    @GetMapping
    public String showPhones(Model model) {
        List<Phone> phones = phoneService.findAll();

        model.addAttribute("phones", phones);
        model.addAttribute(
                "primaryAssignmentsByPhoneId",
                phoneAssignmentService.findPrimaryActiveAssignments(
                        phones.stream().map(Phone::getId).toList()
                )
        );

        return "phones/index";
    }

    @GetMapping("/new")
    public String showNewPhoneForm(Model model) {
        model.addAttribute("form", new PhoneForm());
        addCreateFormOptions(model);
        return "phones/new";
    }

    @PostMapping
    public String registerPhone(
            @Valid @ModelAttribute("form") PhoneForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addCreateFormOptions(model);
            return "phones/new";
        }

        try {
            phoneService.register(form.toRegisterCommand());
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("phone", exception.getMessage());
            addCreateFormOptions(model);
            return "phones/new";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Teléfono registrado correctamente."
        );

        return "redirect:/phones";
    }

    @GetMapping("/{phoneId}")
    public String showPhoneDetail(
            @PathVariable Long phoneId,
            Model model
    ) {
        model.addAttribute("phone", phoneService.findById(phoneId));
        model.addAttribute(
                "activeAssignments",
                phoneAssignmentService.findActiveByPhoneId(phoneId)
        );
        model.addAttribute(
                "assignmentHistory",
                phoneAssignmentService.findHistoryByPhoneId(phoneId)
        );

        return "phones/detail";
    }

    @GetMapping("/{phoneId}/edit")
    public String showEditPhoneForm(
            @PathVariable Long phoneId,
            Model model
    ) {
        Phone phone = phoneService.findById(phoneId);

        model.addAttribute("phoneId", phoneId);
        model.addAttribute("form", PhoneForm.from(phone));
        addEditFormOptions(model, phoneId);

        return "phones/edit";
    }

    @PostMapping("/{phoneId}")
    public String updatePhone(
            @PathVariable Long phoneId,
            @Valid @ModelAttribute("form") PhoneForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("phoneId", phoneId);
            addEditFormOptions(model, phoneId);
            return "phones/edit";
        }

        try {
            phoneService.update(phoneId, form.toUpdateCommand());
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("phone", exception.getMessage());
            model.addAttribute("phoneId", phoneId);
            addEditFormOptions(model, phoneId);
            return "phones/edit";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Teléfono actualizado correctamente."
        );

        return "redirect:/phones/" + phoneId;
    }

    private void addCreateFormOptions(Model model) {
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute("phoneLines", phoneService.findAvailablePhoneLines());
        model.addAttribute("phoneStatuses", PhoneStatus.values());
    }

    private void addEditFormOptions(Model model, Long phoneId) {
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute(
                "phoneLines",
                phoneService.findSelectablePhoneLines(phoneId)
        );
        model.addAttribute("phoneStatuses", PhoneStatus.values());
    }
}