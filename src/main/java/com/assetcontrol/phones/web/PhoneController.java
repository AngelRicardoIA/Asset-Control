package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.PhoneAssignmentService;
import com.assetcontrol.phones.application.PhoneRegistrationService;
import com.assetcontrol.phones.application.PhoneService;
import com.assetcontrol.phones.application.RegisterPhoneWithAssignmentCommand;
import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import com.assetcontrol.phones.domain.PhoneStatus;
import com.assetcontrol.phones.maintenance.application.PhoneMaintenanceService;
import com.assetcontrol.people.application.DuplicatePersonIdentifierException;
import com.assetcontrol.people.application.DuplicatePersonUsernameException;
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
    private final PhoneRegistrationService registrationService;
    private final PhoneAssignmentService phoneAssignmentService;
    private final PhoneMaintenanceService maintenanceService;
    private final com.assetcontrol.sites.application.SiteService siteService;

    public PhoneController(
            PhoneService phoneService,
            PhoneRegistrationService registrationService,
            PhoneAssignmentService phoneAssignmentService,
            PhoneMaintenanceService maintenanceService,
            com.assetcontrol.sites.application.SiteService siteService
    ) {
        this.phoneService = phoneService;
        this.registrationService = registrationService;
        this.phoneAssignmentService = phoneAssignmentService;
        this.maintenanceService = maintenanceService;
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
            validateInitialAssignment(form, bindingResult);

            if (bindingResult.hasErrors()) {
                addCreateFormOptions(model);
                return "phones/new";
            }

            Phone phone = registrationService.register(
                    new RegisterPhoneWithAssignmentCommand(
                            form.toRegisterCommand(),
                            form.getInitialAssignment().hasData()
                                    ? form.getInitialAssignment().toCommand()
                                    : null
                    )
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Teléfono registrado correctamente."
            );

            return "redirect:/phones/" + phone.getId() + "#asignaciones";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("phone", exception.getMessage());
            addCreateFormOptions(model);
            return "phones/new";
        } catch (DuplicatePersonIdentifierException exception) {
            bindingResult.rejectValue(
                    "initialAssignment.newPersonExternalId",
                    "person.identifier.duplicate",
                    exception.getMessage()
            );
            addCreateFormOptions(model);
            return "phones/new";
        } catch (DuplicatePersonUsernameException exception) {
            bindingResult.rejectValue(
                    "initialAssignment.newPersonUsername",
                    "person.username.duplicate",
                    exception.getMessage()
            );
            addCreateFormOptions(model);
            return "phones/new";
        }
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
        model.addAttribute(
                "maintenanceRecords",
                maintenanceService.findByPhoneId(phoneId)
        );
        model.addAttribute(
                "lastClosedAssignment",
                phoneAssignmentService.findLastClosedByPhoneId(phoneId)
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
        model.addAttribute("people", phoneAssignmentService.findPeople());
        model.addAttribute("phoneAssignmentTypes", PhoneAssignmentType.values());
    }

    private void addEditFormOptions(Model model, Long phoneId) {
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute(
                "phoneLines",
                phoneService.findSelectablePhoneLines(phoneId)
        );
        model.addAttribute("phoneStatuses", PhoneStatus.values());
    }

    private void validateInitialAssignment(
            PhoneForm phoneForm,
            BindingResult bindingResult
    ) {
        PhoneAssignmentForm assignment = phoneForm.getInitialAssignment();

        if (!assignment.hasData()) {
            return;
        }

        if (assignment.getPersonId() == null
                && !assignment.hasCompleteNewPersonData()) {
            bindingResult.rejectValue(
                    "initialAssignment.personId",
                    "assignment.person.required",
                    "Selecciona una persona o completa los datos de una nueva."
            );
        }

        if (assignment.getType() == null) {
            bindingResult.rejectValue(
                    "initialAssignment.type",
                    "assignment.type.required",
                    "Selecciona el tipo de movimiento."
            );
        }

        if (assignment.getAssignedAt() == null) {
            bindingResult.rejectValue(
                    "initialAssignment.assignedAt",
                    "assignment.date.required",
                    "Selecciona la fecha de asignación."
            );
        }
    }
}
