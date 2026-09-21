package com.assetcontrol.phones.application;

public record RegisterPhoneWithAssignmentCommand(
        RegisterPhoneCommand phone,
        CreatePhoneAssignmentCommand initialAssignment
) {
}
