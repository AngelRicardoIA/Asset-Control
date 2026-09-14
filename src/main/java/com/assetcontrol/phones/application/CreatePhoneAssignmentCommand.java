package com.assetcontrol.phones.application;

import com.assetcontrol.phones.domain.PhoneAssignmentType;

import java.time.LocalDate;

public record CreatePhoneAssignmentCommand(
        Long personId,
        PhoneAssignmentType type,
        LocalDate assignedAt,
        LocalDate dueAt,
        String observations
) {
}