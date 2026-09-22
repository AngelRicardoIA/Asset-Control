package com.assetcontrol.phones.application;

import java.time.LocalDate;

public record ReturnPhoneAssignmentCommand(
        LocalDate returnedAt,
        String receivedBy,
        String returnNotes
) {
}
