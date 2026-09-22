package com.assetcontrol.assignments.application;

import java.time.LocalDate;

public record CloseComputerAssignmentCommand(
        LocalDate returnedAt,
        String receivedBy,
        String returnNotes
) {
}
