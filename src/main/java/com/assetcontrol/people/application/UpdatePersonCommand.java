package com.assetcontrol.people.application;

public record UpdatePersonCommand(
        String externalId,
        String username,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String managerName
) {
}
