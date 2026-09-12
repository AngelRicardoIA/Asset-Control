package com.assetcontrol.computers.application;

import com.assetcontrol.assignments.application.ComputerAssignmentService;
import com.assetcontrol.assignments.application.CreateComputerAssignmentCommand;
import com.assetcontrol.computers.domain.Computer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComputerRegistrationService {

    private final ComputerService computerService;
    private final ComputerAssignmentService assignmentService;

    public ComputerRegistrationService(
            ComputerService computerService,
            ComputerAssignmentService assignmentService
    ) {
        this.computerService = computerService;
        this.assignmentService = assignmentService;
    }

    public Computer register(RegisterComputerCommand command) {
        Computer computer = computerService.create(command.computer());

        if (command.initialAssignment() != null) {
            CreateComputerAssignmentCommand assignment = command.initialAssignment();

            assignmentService.create(new CreateComputerAssignmentCommand(
                    computer.getId(),
                    assignment.personId(),
                    assignment.newPersonExternalId(),
                    assignment.newPersonUsername(),
                    assignment.newPersonFullName(),
                    assignment.newPersonEmail(),
                    assignment.assignmentType(),
                    assignment.assignedAt(),
                    assignment.dueDate(),
                    assignment.notes()
            ));
        }

        return computer;
    }
}