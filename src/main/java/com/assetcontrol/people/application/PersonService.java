package com.assetcontrol.people.application;

import com.assetcontrol.people.domain.Person;
import com.assetcontrol.people.domain.PersonRepository;
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

        return personRepository.save(person);
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
}
