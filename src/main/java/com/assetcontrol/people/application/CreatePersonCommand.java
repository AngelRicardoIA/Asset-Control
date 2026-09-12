package com.assetcontrol.people.application;

public record CreatePersonCommand(
        String externalId,
        String username,
        String fullName,
        String email
) {
}