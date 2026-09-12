package com.assetcontrol.computers.web;

import com.assetcontrol.assignments.application.ComputerAssignmentService;
import com.assetcontrol.assignments.domain.AssignmentType;
import com.assetcontrol.computers.application.ComputerRegistrationService;
import com.assetcontrol.computers.application.ComputerService;
import com.assetcontrol.computers.application.DuplicateComputerFieldException;
import com.assetcontrol.computers.application.RegisterComputerCommand;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.computers.domain.ComputerType;
import com.assetcontrol.people.application.DuplicatePersonIdentifierException;
import com.assetcontrol.people.application.DuplicatePersonUsernameException;
import com.assetcontrol.people.application.PersonService;
import com.assetcontrol.sites.application.SiteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.assetcontrol.maintenance.application.ComputerMaintenanceService;

import java.util.List;

@Controller
@RequestMapping("/computers")
public class ComputerController {

    private final ComputerService computerService;
    private final ComputerRegistrationService registrationService;
    private final ComputerAssignmentService assignmentService;
    private final SiteService siteService;
    private final PersonService personService;
    private final ComputerMaintenanceService maintenanceService;

    public ComputerController(
            ComputerService computerService,
            ComputerRegistrationService registrationService,
            ComputerAssignmentService assignmentService,
            SiteService siteService,
            PersonService personService,
            ComputerMaintenanceService maintenanceService
    ) {
        this.computerService = computerService;
        this.registrationService = registrationService;
        this.assignmentService = assignmentService;
        this.siteService = siteService;
        this.personService = personService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public String showComputerList(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) ComputerStatus status,
            @RequestParam(required = false) ComputerType type,
            Model model
    ) {
        List<Computer> computers = computerService.search(query, status, type);

        model.addAttribute("computers", computers);
        model.addAttribute(
                "activeAssignmentsByComputerId",
                assignmentService.findActiveByComputerIds(
                        computers.stream().map(Computer::getId).toList()
                )
        );
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
        model.addAttribute(
                "assignments",
                assignmentService.findHistoryByComputerId(id)
        );

        model.addAttribute(
                "maintenanceRecords",
                maintenanceService.findByComputerId(id)
        );

        return "computers/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditComputerForm(@PathVariable Long id, Model model) {
        Computer computer = computerService.findById(id);

        model.addAttribute("computer", computer);
        model.addAttribute("editForm", UpdateComputerForm.from(computer));
        addEditComputerFormData(model);

        return "computers/edit";
    }

    @PostMapping("/{id}")
    public String updateComputer(
            @PathVariable Long id,
            @Valid @ModelAttribute("editForm") UpdateComputerForm editForm,
            BindingResult bindingResult,
            Model model
    ) {
        Computer computer = computerService.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("computer", computer);
            addEditComputerFormData(model);
            return "computers/edit";
        }

        try {
            computerService.update(id, editForm.toCommand());
            return "redirect:/computers/" + id;
        } catch (DuplicateComputerFieldException exception) {
            String message = exception.getField().equals("asset")
                    ? "Ya existe un equipo con este asset."
                    : "Ya existe un equipo con este hostname.";

            bindingResult.rejectValue(
                    exception.getField(),
                    "computer." + exception.getField() + ".duplicate",
                    message
            );

            model.addAttribute("computer", computer);
            addEditComputerFormData(model);
            return "computers/edit";
        }catch (IllegalArgumentException exception) {
            bindingResult.rejectValue(
                    "status",
                    "computer.status.invalid",
                    exception.getMessage()
            );

            model.addAttribute("computer", computer);
            addEditComputerFormData(model);
            return "computers/edit";
        }
    }

    @GetMapping("/new")
    public String showNewComputerForm(Model model) {
        addNewComputerFormData(model);
        model.addAttribute("computer", new ComputerForm());

        return "computers/new";
    }

    @PostMapping
    public String createComputer(
            @Valid @ModelAttribute("computer") ComputerForm computerForm,
            BindingResult bindingResult,
            Model model
    ) {
        validateInitialAssignment(computerForm, bindingResult);

        if (bindingResult.hasErrors()) {
            addNewComputerFormData(model);
            return "computers/new";
        }

        try {
            registrationService.register(new RegisterComputerCommand(
                    computerForm.toCommand(),
                    computerForm.getInitialAssignment().hasData()
                            ? computerForm.getInitialAssignment().toCommand(null)
                            : null
            ));

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
        } catch (DuplicatePersonIdentifierException exception) {
            bindingResult.rejectValue(
                    "initialAssignment.newPersonExternalId",
                    "person.identifier.duplicate",
                    exception.getMessage()
            );
        } catch (DuplicatePersonUsernameException exception) {
            bindingResult.rejectValue(
                    "initialAssignment.newPersonUsername",
                    "person.username.duplicate",
                    exception.getMessage()
            );
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue(
                    "initialAssignment.dueDate",
                    "assignment.invalid",
                    exception.getMessage()
            );
        }

        addNewComputerFormData(model);
        return "computers/new";
    }

    private void addNewComputerFormData(Model model) {
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute("people", personService.findAll());
        model.addAttribute("computerTypes", ComputerType.values());
        model.addAttribute("assignmentTypes", AssignmentType.values());
    }

    private void validateInitialAssignment(
            ComputerForm computerForm,
            BindingResult bindingResult
    ) {
        InitialAssignmentForm assignment = computerForm.getInitialAssignment();

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

        if (assignment.getAssignmentType() == null) {
            bindingResult.rejectValue(
                    "initialAssignment.assignmentType",
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

    private void addEditComputerFormData(Model model) {
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute("computerTypes", ComputerType.values());
        model.addAttribute("computerStatuses", ComputerStatus.values());
    }
}