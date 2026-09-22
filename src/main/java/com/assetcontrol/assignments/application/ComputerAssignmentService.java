package com.assetcontrol.assignments.application;

import com.assetcontrol.assignments.domain.AssignmentType;
import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.application.ComputerNotFoundException;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.people.application.CreatePersonCommand;
import com.assetcontrol.people.application.DuplicatePersonIdentifierException;
import com.assetcontrol.people.application.PersonService;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.people.domain.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.assetcontrol.people.application.DuplicatePersonUsernameException;

import java.time.LocalDate;
import java.util.List;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComputerAssignmentService {

    private final ComputerAssignmentRepository assignmentRepository;
    private final ComputerRepository computerRepository;
    private final PersonRepository personRepository;
    private final PersonService personService;

    public ComputerAssignmentService(
            ComputerAssignmentRepository assignmentRepository,
            ComputerRepository computerRepository,
            PersonRepository personRepository,
            PersonService personService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.computerRepository = computerRepository;
        this.personRepository = personRepository;
        this.personService = personService;
    }

    public List<ComputerAssignment> findHistoryByComputerId(Long computerId) {
        return assignmentRepository.findHistoryByComputerId(computerId);
    }

    public List<ComputerAssignment> findHistoryByPersonId(Long personId) {
        return assignmentRepository.findHistoryByPersonIdWithComputer(personId);
    }

    @Transactional
    public ComputerAssignment create(CreateComputerAssignmentCommand command) {
        Computer computer = computerRepository.findById(command.computerId())
                .orElseThrow(() -> new ComputerNotFoundException(command.computerId()));

        Person person = resolvePerson(command);

        if (computer.getStatus() == ComputerStatus.RETIRED) {
            throw new IllegalArgumentException(
                    "No se puede asignar un equipo dado de baja."
            );
        }

        if (assignmentRepository.existsByComputer_IdAndPerson_IdAndReturnedAtIsNull(
                computer.getId(),
                person.getId()
        )) {
            throw new DuplicateActiveAssignmentException();
        }

        LocalDate assignedAt = command.assignedAt() == null
                ? LocalDate.now()
                : command.assignedAt();

        LocalDate dueDate = validateDueDate(
                command.assignmentType(),
                assignedAt,
                command.dueDate()
        );

        ComputerAssignment assignment = new ComputerAssignment(
                computer,
                person,
                command.assignmentType(),
                assignedAt,
                dueDate,
                normalizeOptional(command.notes())
        );

        ComputerAssignment savedAssignment = assignmentRepository.save(assignment);

        synchronizeComputerStatus(computer);

        return savedAssignment;
    }

    private Person resolvePerson(CreateComputerAssignmentCommand command) {
        if (command.personId() != null) {
            return personRepository.findById(command.personId())
                    .orElseThrow(() -> new PersonNotFoundException(command.personId()));
        }

        return personService.create(new CreatePersonCommand(
                command.newPersonExternalId(),
                command.newPersonUsername(),
                command.newPersonFullName(),
                command.newPersonEmail()
        ));
    }

    private LocalDate validateDueDate(
            AssignmentType assignmentType,
            LocalDate assignedAt,
            LocalDate dueDate
    ) {
        if (assignmentType == AssignmentType.ASSIGNMENT) {
            return null;
        }

        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "Un préstamo requiere fecha de vencimiento."
            );
        }

        if (dueDate.isBefore(assignedAt)) {
            throw new IllegalArgumentException(
                    "La fecha de vencimiento no puede ser anterior a la asignación."
            );
        }

        return dueDate;
    }

    private void synchronizeComputerStatus(Computer computer) {
        if (computer.getStatus() == ComputerStatus.RETIRED) {
            return;
        }

        Long computerId = computer.getId();
        if (assignmentRepository.existsByComputer_IdAndAssignmentTypeAndReturnedAtIsNull(
                computerId,
                AssignmentType.LOAN
        )) {
            computer.changeStatus(ComputerStatus.LOANED);
            return;
        }

        if (assignmentRepository.existsByComputer_IdAndReturnedAtIsNull(computerId)) {
            computer.changeStatus(ComputerStatus.ASSIGNED);
            return;
        }

        computer.changeStatus(ComputerStatus.AVAILABLE);
    }

    @Transactional
    public void refreshStatuses(Collection<Computer> computers) {
        computers.forEach(this::synchronizeComputerStatus);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");

        return normalized.isBlank() ? null : normalized;
    }

    @Transactional
    public void close(
            Long computerId,
            Long assignmentId,
            CloseComputerAssignmentCommand command
    ) {
        ComputerAssignment assignment = assignmentRepository.findDetailedById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        if (!assignment.getComputer().getId().equals(computerId)) {
            throw new IllegalArgumentException(
                    "La asignación no pertenece al equipo indicado."
            );
        }

        LocalDate returnedAt = command.returnedAt() == null
                ? LocalDate.now()
                : command.returnedAt();

        assignment.close(
                returnedAt,
                normalizeRequired(command.receivedBy(), "quién recibe"),
                normalizeOptional(command.returnNotes())
        );

        synchronizeComputerStatus(assignment.getComputer());
    }

    private String normalizeRequired(String value, String field) {
        String normalized = normalizeOptional(value);

        if (normalized == null) {
            throw new IllegalArgumentException("Indica " + field + " el equipo.");
        }

        return normalized;
    }

    public Map<Long, List<ComputerAssignment>> findActiveByComputerIds(
            Collection<Long> computerIds
    ) {
        if (computerIds.isEmpty()) {
            return Map.of();
        }

        return assignmentRepository.findActiveByComputerIdsWithPerson(computerIds)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getComputer().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    public Map<Long, ComputerAssignment> findLastClosedByComputerIds(
            Collection<Long> computerIds
    ) {
        if (computerIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, ComputerAssignment> lastAssignments = new LinkedHashMap<>();

        for (ComputerAssignment assignment
                : assignmentRepository.findClosedByComputerIdsWithPerson(computerIds)) {
            lastAssignments.putIfAbsent(
                    assignment.getComputer().getId(),
                    assignment
            );
        }

        return lastAssignments;
    }

    public ComputerAssignment findLastClosedByComputerId(Long computerId) {
        return assignmentRepository.findClosedByComputerIdsWithPerson(
                List.of(computerId)
        ).stream().findFirst().orElse(null);
    }

    public Page<Person> findPeopleWithActiveAssignmentsPage(
            String query,
            Long siteId,
            boolean chronological,
            boolean descending,
            int page
    ) {
        return assignmentRepository.findPeopleWithActiveAssignmentsPage(
                query == null ? "" : query.trim(),
                siteId,
                chronological,
                descending,
                PageRequest.of(page, 10)
        );
    }

    public Map<Long, List<ComputerAssignment>> findActiveByPersonIds(
            Collection<Long> personIds
    ) {
        if (personIds.isEmpty()) {
            return Map.of();
        }

        return assignmentRepository.findActiveByPersonIdsWithComputer(personIds)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getPerson().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
