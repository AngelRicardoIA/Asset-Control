package com.assetcontrol.assignments.application;

import com.assetcontrol.assignments.domain.AssignmentType;

import java.time.LocalDate;

public record CreateComputerAssignmentCommand(
        Long computerId,
        Long personId,
        AssignmentType assignmentType,
        LocalDate assignedAt,
        LocalDate dueDate,
        String notes
) {
}