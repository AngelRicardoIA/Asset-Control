package com.assetcontrol.people.application;

public record CreatePersonCommand(
        String externalId,
        String fullName,
        String email
) {
}