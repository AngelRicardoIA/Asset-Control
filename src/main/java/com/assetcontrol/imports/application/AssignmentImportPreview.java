package com.assetcontrol.imports.application;

import java.io.Serializable;
import java.util.List;

public record AssignmentImportPreview(
        Kind kind,
        List<Row> rows
) implements Serializable {

    public enum Kind {
        COMPUTER,
        PHONE
    }

    public record Row(
            String assetIdentifier,
            String username,
            String assignmentType,
            String assignedAt,
            String dueAt,
            String returnedAt,
            String notes
    ) implements Serializable {
    }
}
