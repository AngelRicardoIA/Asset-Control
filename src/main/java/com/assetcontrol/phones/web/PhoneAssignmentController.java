package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.PhoneAssignmentService;
import com.assetcontrol.phones.application.PhoneService;
import com.assetcontrol.phones.application.ReturnPhoneAssignmentCommand;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
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
import com.assetcontrol.shared.web.AssetReturnForm;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        validatePersonSelection(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("phone", phoneService.findById(phoneId));
            model.addAttribute("phoneId", phoneId);
            addFormOptions(model);
            return "phones/assignments/new";
        }

        try {
            phoneAssignmentService.assign(phoneId, form.toCommand());
        } catch (DuplicatePersonIdentifierException exception) {
            bindingResult.rejectValue(
                    "newPersonExternalId",
                    "person.identifier.duplicate",
                    exception.getMessage()
            );
            model.addAttribute("phone", phoneService.findById(phoneId));
            model.addAttribute("phoneId", phoneId);
            addFormOptions(model);
            return "phones/assignments/new";
        } catch (DuplicatePersonUsernameException exception) {
            bindingResult.rejectValue(
                    "newPersonUsername",
                    "person.username.duplicate",
                    exception.getMessage()
            );
            model.addAttribute("phone", phoneService.findById(phoneId));
            model.addAttribute("phoneId", phoneId);
            addFormOptions(model);
            return "phones/assignments/new";
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

    @PostMapping("/phones/{phoneId}/assignments/{assignmentId}/return")
    public String returnPhone(
            @PathVariable Long phoneId,
            @PathVariable Long assignmentId,
            @ModelAttribute AssetReturnForm returnForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var assignment = phoneAssignmentService.returnAssignment(
                    phoneId,
                    assignmentId,
                    new ReturnPhoneAssignmentCommand(
                            returnForm.getReturnedAt(),
                            returnForm.getReceivedBy(),
                            returnForm.getReturnNotes()
                    )
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Asignación devuelta correctamente."
            );
            return "redirect:/phones/" + assignment.getPhone().getId();
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/phones/" + phoneId + "#asignacion-" + assignmentId;
    }

    private void addFormOptions(Model model) {
        model.addAttribute("people", phoneAssignmentService.findPeople());
        model.addAttribute("assignmentTypes", PhoneAssignmentType.values());
    }

    private void validatePersonSelection(
            PhoneAssignmentForm form,
            BindingResult bindingResult
    ) {
        if (form.getPersonId() != null) {
            return;
        }

        if (!form.hasNewPersonData()) {
            bindingResult.rejectValue(
                    "personId",
                    "assignment.person.required",
                    "Selecciona una persona o registra una nueva."
            );
            return;
        }

        if (form.getNewPersonUsername() == null
                || form.getNewPersonUsername().isBlank()) {
            bindingResult.rejectValue(
                    "newPersonUsername",
                    "person.username.required",
                    "El nombre de usuario es obligatorio."
            );
        }

        if (!form.hasCompleteNewPersonData()) {
            if (form.getNewPersonExternalId() == null
                    || form.getNewPersonExternalId().isBlank()) {
                bindingResult.rejectValue(
                        "newPersonExternalId",
                        "person.identifier.required",
                        "El ID es obligatorio."
                );
            }

            if (form.getNewPersonFullName() == null
                    || form.getNewPersonFullName().isBlank()) {
                bindingResult.rejectValue(
                        "newPersonFullName",
                        "person.name.required",
                        "El nombre es obligatorio."
                );
            }

            if (form.getNewPersonEmail() == null
                    || form.getNewPersonEmail().isBlank()) {
                bindingResult.rejectValue(
                        "newPersonEmail",
                        "person.email.required",
                        "El correo es obligatorio."
                );
            }
        }
    }
}
