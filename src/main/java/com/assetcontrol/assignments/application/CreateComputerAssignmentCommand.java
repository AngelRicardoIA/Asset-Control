package com.assetcontrol.assignments.application;

import com.assetcontrol.assignments.domain.AssignmentType;

import java.time.LocalDate;

public record CreateComputerAssignmentCommand(
        Long computerId,
        Long personId,
        String newPersonExternalId,
        String newPersonUsername,
        String newPersonFullName,
        String newPersonEmail,
        AssignmentType assignmentType,
        LocalDate assignedAt,
        LocalDate dueDate,
        String notes
) {
}