package com.assetcontrol.people.application;

import com.assetcontrol.assignments.application.ComputerAssignmentService;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.phones.application.PhoneAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PersonProfileService {

    private final PersonService personService;
    private final ComputerAssignmentService computerAssignmentService;
    private final PhoneAssignmentService phoneAssignmentService;

    public PersonProfileService(
            PersonService personService,
            ComputerAssignmentService computerAssignmentService,
            PhoneAssignmentService phoneAssignmentService
    ) {
        this.personService = personService;
        this.computerAssignmentService = computerAssignmentService;
        this.phoneAssignmentService = phoneAssignmentService;
    }

    public PersonAssetProfile findByPersonId(Long personId) {
        Person person = personService.findById(personId);

        return new PersonAssetProfile(
                person,
                computerAssignmentService.findHistoryByPersonId(personId),
                phoneAssignmentService.findHistoryByPersonId(personId)
        );
    }
}
