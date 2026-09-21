package com.assetcontrol.phones.application;

import com.assetcontrol.phones.domain.Phone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PhoneRegistrationService {

    private final PhoneService phoneService;
    private final PhoneAssignmentService assignmentService;

    public PhoneRegistrationService(
            PhoneService phoneService,
            PhoneAssignmentService assignmentService
    ) {
        this.phoneService = phoneService;
        this.assignmentService = assignmentService;
    }

    public Phone register(RegisterPhoneWithAssignmentCommand command) {
        Phone phone = phoneService.register(command.phone());

        if (command.initialAssignment() != null) {
            CreatePhoneAssignmentCommand assignment = command.initialAssignment();
            assignmentService.assign(phone.getId(), assignment);
        }

        return phone;
    }
}
