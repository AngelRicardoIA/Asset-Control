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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");

        return normalized.isBlank() ? null : normalized;
    }
}