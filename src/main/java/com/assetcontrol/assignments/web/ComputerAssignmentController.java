package com.assetcontrol.assignments.web;

import com.assetcontrol.assignments.application.ComputerAssignmentService;
import com.assetcontrol.assignments.application.DuplicateActiveAssignmentException;
import com.assetcontrol.assignments.domain.AssignmentType;
import com.assetcontrol.computers.application.ComputerService;
import com.assetcontrol.people.application.DuplicatePersonIdentifierException;
import com.assetcontrol.people.application.PersonService;
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
@RequestMapping("/computers/{computerId}/assignments")
public class ComputerAssignmentController {

    private final ComputerAssignmentService assignmentService;
    private final ComputerService computerService;
    private final PersonService personService;

    public ComputerAssignmentController(
            ComputerAssignmentService assignmentService,
            ComputerService computerService,
            PersonService personService
    ) {
        this.assignmentService = assignmentService;
        this.computerService = computerService;
        this.personService = personService;
    }

    @GetMapping("/new")
    public String showNewAssignmentForm(
            @PathVariable Long computerId,
            Model model
    ) {
        prepareFormModel(computerId, model);
        model.addAttribute("assignment", new ComputerAssignmentForm());
        return "assignments/new";
    }

    @PostMapping
    public String createAssignment(
            @PathVariable Long computerId,
            @Valid @ModelAttribute("assignment") ComputerAssignmentForm assignmentForm,
            BindingResult bindingResult,
            Model model
    ) {
        validatePersonSelection(assignmentForm, bindingResult);

        if (bindingResult.hasErrors()) {
            prepareFormModel(computerId, model);
            return "assignments/new";
        }

        try {
            assignmentService.create(assignmentForm.toCommand(computerId));
            return "redirect:/computers/" + computerId;
        } catch (DuplicateActiveAssignmentException exception) {
            bindingResult.rejectValue(
                    "personId",
                    "assignment.duplicate",
                    exception.getMessage()
            );
        } catch (DuplicatePersonIdentifierException exception) {
            bindingResult.rejectValue(
                    "newPersonExternalId",
                    "person.identifier.duplicate",
                    exception.getMessage()
            );
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue(
                    "dueDate",
                    "assignment.invalid",
                    exception.getMessage()
            );
        }

        prepareFormModel(computerId, model);
        return "assignments/new";
    }

    private void prepareFormModel(Long computerId, Model model) {
        model.addAttribute("computer", computerService.findById(computerId));
        model.addAttribute("people", personService.findAll());
        model.addAttribute("assignmentTypes", AssignmentType.values());
    }

    private void validatePersonSelection(
            ComputerAssignmentForm assignmentForm,
            BindingResult bindingResult
    ) {
        if (assignmentForm.getPersonId() != null) {
            return;
        }

        if (!assignmentForm.hasNewPersonData()) {
            bindingResult.rejectValue(
                    "personId",
                    "assignment.person.required",
                    "Selecciona una persona o registra una nueva."
            );
            return;
        }

        if (!assignmentForm.hasCompleteNewPersonData()) {
            if (assignmentForm.getNewPersonExternalId() == null
                    || assignmentForm.getNewPersonExternalId().isBlank()) {
                bindingResult.rejectValue(
                        "newPersonExternalId",
                        "person.identifier.required",
                        "El ID es obligatorio."
                );
            }

            if (assignmentForm.getNewPersonFullName() == null
                    || assignmentForm.getNewPersonFullName().isBlank()) {
                bindingResult.rejectValue(
                        "newPersonFullName",
                        "person.name.required",
                        "El nombre es obligatorio."
                );
            }

            if (assignmentForm.getNewPersonEmail() == null
                    || assignmentForm.getNewPersonEmail().isBlank()) {
                bindingResult.rejectValue(
                        "newPersonEmail",
                        "person.email.required",
                        "El correo es obligatorio."
                );
            }
        }
    }
}