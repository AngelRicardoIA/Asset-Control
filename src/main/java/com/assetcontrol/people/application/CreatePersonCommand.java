package com.assetcontrol.people.application;

public record CreatePersonCommand(
        String externalId,
        String username,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String managerName
) {
    public CreatePersonCommand(
            String externalId,
            String username,
            String fullName,
            String email
    ) {
        this(externalId, username, fullName, email, null, null, null);
    }
}
