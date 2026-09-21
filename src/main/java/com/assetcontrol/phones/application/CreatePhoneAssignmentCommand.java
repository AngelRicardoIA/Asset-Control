package com.assetcontrol.phones.application;

import com.assetcontrol.phones.domain.PhoneAssignmentType;

import java.time.LocalDate;

public record CreatePhoneAssignmentCommand(
        Long personId,
        String newPersonExternalId,
        String newPersonUsername,
        String newPersonFullName,
        String newPersonEmail,
        PhoneAssignmentType type,
        LocalDate assignedAt,
        LocalDate dueAt,
        String observations
) {
}
