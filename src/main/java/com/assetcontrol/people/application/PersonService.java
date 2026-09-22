package com.assetcontrol.people.application;

import com.assetcontrol.people.domain.Person;
import com.assetcontrol.people.domain.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAllByOrderByFullNameAsc();
    }

    public Person findById(Long personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException(personId));
    }

    public Page<Person> searchPage(
            String query,
            boolean chronological,
            boolean descending,
            int page
    ) {
        return personRepository.searchPage(
                query == null ? "" : query.trim(),
                chronological,
                descending,
                PageRequest.of(page, 10)
        );
    }

    private String normalizeUsername(String value) {
        return normalizeRequired(value, "nombre de usuario")
                .toLowerCase(Locale.ROOT);
    }

    @Transactional
    public Person create(CreatePersonCommand command) {
        String externalId = normalizeIdentifier(command.externalId());
        String username = normalizeUsername(command.username());

        if (personRepository.existsByExternalIdIgnoreCase(externalId)) {
            throw new DuplicatePersonIdentifierException(externalId);
        }

        if (personRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicatePersonUsernameException(username);
        }

        Person person = new Person(
                externalId,
                username,
                normalizeRequired(command.fullName(), "nombre"),
                normalizeEmail(command.email())
        );

        person.updateDetails(
                externalId,
                username,
                normalizeRequired(command.fullName(), "nombre"),
                normalizeEmail(command.email()),
                normalizeOptional(command.jobTitle()),
                normalizeOptional(command.department()),
                normalizeOptional(command.managerName())
        );

        return personRepository.save(person);
    }

    @Transactional
    public Person update(Long personId, UpdatePersonCommand command) {
        Person person = findById(personId);
        String externalId = normalizeIdentifier(command.externalId());
        String username = normalizeUsername(command.username());

        if (personRepository.existsByExternalIdIgnoreCaseAndIdNot(externalId, personId)) {
            throw new DuplicatePersonIdentifierException(externalId);
        }

        if (personRepository.existsByUsernameIgnoreCaseAndIdNot(username, personId)) {
            throw new DuplicatePersonUsernameException(username);
        }

        person.updateDetails(
                externalId,
                username,
                normalizeRequired(command.fullName(), "nombre"),
                normalizeEmail(command.email()),
                normalizeOptional(command.jobTitle()),
                normalizeOptional(command.department()),
                normalizeOptional(command.managerName())
        );

        return person;
    }

    private String normalizeIdentifier(String value) {
        return normalizeRequired(value, "ID").toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String value) {
        return normalizeRequired(value, "correo").toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value, String field) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("El " + field + " es obligatorio.");
        }

        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }
}
