package com.assetcontrol.computers.application;

import com.assetcontrol.assignments.application.CreateComputerAssignmentCommand;

public record RegisterComputerCommand(
        CreateComputerCommand computer,
        CreateComputerAssignmentCommand initialAssignment
) {
}