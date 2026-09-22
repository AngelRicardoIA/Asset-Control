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
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.shared.web.InventoryPagination;
import com.assetcontrol.shared.web.InventorySort;
import com.assetcontrol.shared.web.InventorySortDirection;
import com.assetcontrol.shared.web.InventoryView;
import com.assetcontrol.sites.application.SiteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
import com.assetcontrol.maintenance.application.MaintenanceOverviewService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/computers")
public class ComputerController {

    private final ComputerService computerService;
    private final ComputerRegistrationService registrationService;
    private final ComputerAssignmentService assignmentService;
    private final SiteService siteService;
    private final PersonService personService;
    private final ComputerMaintenanceService maintenanceService;
    private final MaintenanceOverviewService maintenanceOverview;

    public ComputerController(
            ComputerService computerService,
            ComputerRegistrationService registrationService,
            ComputerAssignmentService assignmentService,
            SiteService siteService,
            PersonService personService,
            ComputerMaintenanceService maintenanceService,
            MaintenanceOverviewService maintenanceOverview
    ) {
        this.computerService = computerService;
        this.registrationService = registrationService;
        this.assignmentService = assignmentService;
        this.siteService = siteService;
        this.personService = personService;
        this.maintenanceService = maintenanceService;
        this.maintenanceOverview = maintenanceOverview;
    }

    @GetMapping
    public String showComputerList(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) ComputerStatus status,
            @RequestParam(required = false) ComputerType type,
            @RequestParam(required = false) Long siteId,
            @RequestParam(defaultValue = "EQUIPMENT") InventoryView view,
            @RequestParam(defaultValue = "ALPHABETICAL") InventorySort sort,
            @RequestParam(defaultValue = "ASCENDING") InventorySortDirection direction,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        boolean chronological = sort == InventorySort.CHRONOLOGICAL;
        boolean descending = direction == InventorySortDirection.DESCENDING;

        if (view == InventoryView.EQUIPMENT) {
            Page<Computer> result = InventoryPagination.load(
                    page,
                    index -> computerService.searchPage(
                            query, status, type, siteId, chronological, descending, index
                    )
            );
            List<Computer> computers = result.getContent();
            model.addAttribute("pagination", new InventoryPagination(result));

            List<Long> availableComputerIds = computers.stream()
                    .filter(computer -> computer.getStatus() == ComputerStatus.AVAILABLE)
                    .map(Computer::getId)
                    .toList();

            model.addAttribute("computers", computers);
            model.addAttribute("maintenanceDueById", maintenanceOverview.overview().computers().stream().collect(
                    java.util.stream.Collectors.toMap(item -> item.computer().getId(), item -> item)
            ));
            model.addAttribute(
                    "activeAssignmentsByComputerId",
                    assignmentService.findActiveByComputerIds(
                            computers.stream().map(Computer::getId).toList()
                    )
            );
            model.addAttribute(
                    "lastClosedAssignmentByComputerId",
                    assignmentService.findLastClosedByComputerIds(availableComputerIds)
            );
            model.addAttribute("peopleWithAssets", List.of());
            model.addAttribute("activeAssignmentsByPersonId", Map.of());
        } else {
            Page<Person> result = InventoryPagination.load(
                    page,
                    index -> assignmentService.findPeopleWithActiveAssignmentsPage(
                            query, siteId, chronological, descending, index
                    )
            );
            List<Person> people = result.getContent();
            model.addAttribute("pagination", new InventoryPagination(result));

            model.addAttribute("computers", List.of());
            model.addAttribute("maintenanceDueById", Map.of());
            model.addAttribute("activeAssignmentsByComputerId", Map.of());
            model.addAttribute("lastClosedAssignmentByComputerId", Map.of());
            model.addAttribute("peopleWithAssets", people);
            model.addAttribute(
                    "activeAssignmentsByPersonId",
                    assignmentService.findActiveByPersonIds(
                            people.stream().map(Person::getId).toList()
                    )
            );
        }

        model.addAttribute("query", query);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedSiteId", siteId);
        model.addAttribute("view", view);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedDirection", direction);
        model.addAttribute("sortOptions", InventorySort.values());
        model.addAttribute("directionOptions", InventorySortDirection.values());
        model.addAttribute("sites", siteService.findAllActive());
        model.addAttribute("computerStatuses", ComputerStatus.values());
        model.addAttribute("computerTypes", ComputerType.values());

        return "computers/index";
    }

    @GetMapping("/{id}")
    public String showComputerDetail(@PathVariable Long id, Model model) {
        var computer = computerService.findById(id);
        model.addAttribute("computer", computer);
        model.addAttribute("maintenanceDue", computer.getStatus() == ComputerStatus.RETIRED ? null : maintenanceOverview.findByComputerId(id));
        model.addAttribute("maintenanceScheduleHistory", maintenanceOverview.scheduleHistory(id));
        model.addAttribute(
                "assignments",
                assignmentService.findHistoryByComputerId(id)
        );

        model.addAttribute(
                "maintenanceRecords",
                maintenanceService.findByComputerId(id)
        );

        model.addAttribute(
                "lastClosedAssignment",
                assignmentService.findLastClosedByComputerId(id)
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
            Computer savedComputer = registrationService.register(new RegisterComputerCommand(
                    computerForm.toCommand(),
                    computerForm.getInitialAssignment().hasData()
                            ? computerForm.getInitialAssignment().toCommand(null)
                            : null
            ));

            return "redirect:/computers/" + savedComputer.getId() + "#asignaciones";
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
