package com.assetcontrol.phones.application;

import com.assetcontrol.people.domain.Person;
import com.assetcontrol.people.domain.PersonRepository;
import com.assetcontrol.people.application.CreatePersonCommand;
import com.assetcontrol.people.application.PersonService;
import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneAssignment;
import com.assetcontrol.phones.domain.PhoneAssignmentRepository;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import com.assetcontrol.phones.domain.PhoneRepository;
import com.assetcontrol.phones.domain.PhoneStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PhoneAssignmentService {

    private final PhoneRepository phoneRepository;
    private final PersonRepository personRepository;
    private final PersonService personService;
    private final PhoneAssignmentRepository phoneAssignmentRepository;

    public PhoneAssignmentService(
            PhoneRepository phoneRepository,
            PersonRepository personRepository,
            PersonService personService,
            PhoneAssignmentRepository phoneAssignmentRepository
    ) {
        this.phoneRepository = phoneRepository;
        this.personRepository = personRepository;
        this.personService = personService;
        this.phoneAssignmentRepository = phoneAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Person> findPeople() {
        return personRepository.findAll(
                Sort.by(Sort.Order.asc("username"))
        );
    }

    @Transactional(readOnly = true)
    public List<PhoneAssignment> findActiveByPhoneId(Long phoneId) {
        return phoneAssignmentRepository.findActiveByPhoneIdWithPerson(phoneId);
    }

    @Transactional(readOnly = true)
    public List<PhoneAssignment> findHistoryByPhoneId(Long phoneId) {
        return phoneAssignmentRepository.findHistoryByPhoneIdWithPerson(phoneId);
    }

    @Transactional(readOnly = true)
    public List<PhoneAssignment> findHistoryByPersonId(Long personId) {
        return phoneAssignmentRepository.findHistoryByPersonIdWithPhone(personId);
    }

    @Transactional(readOnly = true)
    public Map<Long, PhoneAssignment> findPrimaryActiveAssignments(
            Collection<Long> phoneIds
    ) {
        if (phoneIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, PhoneAssignment> assignments = new LinkedHashMap<>();

        for (PhoneAssignment assignment
                : phoneAssignmentRepository.findActiveByPhoneIdsWithPerson(phoneIds)) {
            assignments.putIfAbsent(assignment.getPhone().getId(), assignment);
        }

        return assignments;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<PhoneAssignment>> findActiveByPhoneIds(
            Collection<Long> phoneIds
    ) {
        if (phoneIds.isEmpty()) {
            return Map.of();
        }

        return phoneAssignmentRepository.findActiveByPhoneIdsWithPerson(phoneIds)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getPhone().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Transactional(readOnly = true)
    public Map<Long, PhoneAssignment> findLastClosedByPhoneIds(
            Collection<Long> phoneIds
    ) {
        if (phoneIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, PhoneAssignment> assignments = new LinkedHashMap<>();

        for (PhoneAssignment assignment
                : phoneAssignmentRepository.findClosedByPhoneIdsWithPerson(phoneIds)) {
            assignments.putIfAbsent(assignment.getPhone().getId(), assignment);
        }

        return assignments;
    }

    @Transactional(readOnly = true)
    public PhoneAssignment findLastClosedByPhoneId(Long phoneId) {
        return phoneAssignmentRepository.findClosedByPhoneIdsWithPerson(
                List.of(phoneId)
        ).stream().findFirst().orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Person> findPeopleWithActiveAssignments(
            String query,
            Long siteId
    ) {
        return phoneAssignmentRepository.findPeopleWithActiveAssignments(
                query == null ? "" : query.trim(),
                siteId
        );
    }

    @Transactional(readOnly = true)
    public Map<Long, List<PhoneAssignment>> findActiveByPersonIds(
            Collection<Long> personIds
    ) {
        if (personIds.isEmpty()) {
            return Map.of();
        }

        return phoneAssignmentRepository.findActiveByPersonIdsWithPhone(personIds)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getPerson().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    public PhoneAssignment assign(
            Long phoneId,
            CreatePhoneAssignmentCommand command
    ) {
        Phone phone = phoneRepository.findById(phoneId)
                .orElseThrow(() -> new PhoneNotFoundException(phoneId));

        if (phone.getStatus() == PhoneStatus.RETIRED) {
            throw new IllegalArgumentException(
                    "No se puede asignar un teléfono dado de baja."
            );
        }

        Person person = resolvePerson(command);

        if (phoneAssignmentRepository.existsByPhoneIdAndPersonIdAndReturnedAtIsNull(
                phoneId,
                person.getId()
        )) {
            throw new IllegalArgumentException(
                    "Ese teléfono ya tiene una asignación activa para esta persona."
            );
        }

        PhoneAssignmentType type = command.type() == null
                ? PhoneAssignmentType.ASSIGNMENT
                : command.type();

        LocalDate assignedAt = command.assignedAt() == null
                ? LocalDate.now()
                : command.assignedAt();

        LocalDate dueAt = resolveDueDate(type, assignedAt, command.dueAt());

        PhoneAssignment assignment = new PhoneAssignment(
                phone,
                person,
                type,
                assignedAt,
                dueAt,
                normalizeOptional(command.observations())
        );

        phone.changeStatus(
                type == PhoneAssignmentType.LOAN
                        ? PhoneStatus.LOANED
                        : PhoneStatus.ASSIGNED
        );

        return phoneAssignmentRepository.save(assignment);
    }

    public PhoneAssignment returnAssignment(
            Long phoneId,
            Long assignmentId,
            ReturnPhoneAssignmentCommand command
    ) {
        PhoneAssignment assignment = phoneAssignmentRepository
                .findByIdWithPhoneAndPerson(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "La asignación seleccionada no existe."
                ));

        if (!assignment.getPhone().getId().equals(phoneId)) {
            throw new IllegalArgumentException(
                    "La asignación no pertenece al teléfono indicado."
            );
        }

        if (assignment.getReturnedAt() != null) {
            throw new IllegalArgumentException(
                    "La asignación ya fue devuelta anteriormente."
            );
        }

        LocalDate returnDate = command.returnedAt() == null
                ? LocalDate.now()
                : command.returnedAt();

        if (returnDate.isBefore(assignment.getAssignedAt())) {
            throw new IllegalArgumentException(
                    "La fecha de devolución no puede ser anterior a la asignación."
            );
        }

        assignment.returnOn(
                returnDate,
                normalizeRequired(command.receivedBy(), "quién recibe"),
                normalizeOptional(command.returnNotes())
        );
        synchronizePhoneStatus(assignment.getPhone());

        return assignment;
    }

    private String normalizeRequired(String value, String field) {
        String normalized = normalizeOptional(value);

        if (normalized == null) {
            throw new IllegalArgumentException("Indica " + field + " el teléfono.");
        }

        return normalized;
    }

    private Person resolvePerson(CreatePhoneAssignmentCommand command) {
        if (command.personId() != null) {
            return personRepository.findById(command.personId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La persona seleccionada no existe."
                    ));
        }

        return personService.create(new CreatePersonCommand(
                command.newPersonExternalId(),
                command.newPersonUsername(),
                command.newPersonFullName(),
                command.newPersonEmail()
        ));
    }

    private void synchronizePhoneStatus(Phone phone) {
        if (phone.getStatus() == PhoneStatus.RETIRED) {
            return;
        }

        List<PhoneAssignment> activeAssignments =
                phoneAssignmentRepository.findActiveByPhoneIdWithPerson(phone.getId());

        if (activeAssignments.isEmpty()) {
            phone.changeStatus(PhoneStatus.AVAILABLE);
            return;
        }

        boolean hasLoan = activeAssignments.stream()
                .anyMatch(assignment -> assignment.getType() == PhoneAssignmentType.LOAN);

        phone.changeStatus(hasLoan ? PhoneStatus.LOANED : PhoneStatus.ASSIGNED);
    }

    public void refreshStatuses(Collection<Phone> phones) {
        phones.forEach(this::synchronizePhoneStatus);
    }

    private LocalDate resolveDueDate(
            PhoneAssignmentType type,
            LocalDate assignedAt,
            LocalDate dueAt
    ) {
        if (type != PhoneAssignmentType.LOAN) {
            return null;
        }

        if (dueAt == null) {
            throw new IllegalArgumentException(
                    "Indica cuándo vence el préstamo."
            );
        }

        if (dueAt.isBefore(assignedAt)) {
            throw new IllegalArgumentException(
                    "El vencimiento no puede ser anterior a la asignación."
            );
        }

        return dueAt;
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
