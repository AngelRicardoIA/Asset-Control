package com.assetcontrol.people.application;

import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.phones.domain.PhoneAssignment;

import java.util.List;

public class PersonAssetProfile {

    private final Person person;
    private final List<ComputerAssignment> computerAssignmentHistory;
    private final List<PhoneAssignment> phoneAssignmentHistory;
    private final List<ComputerAssignment> activeComputerAssignments;
    private final List<PhoneAssignment> activePhoneAssignments;

    public PersonAssetProfile(
            Person person,
            List<ComputerAssignment> computerAssignmentHistory,
            List<PhoneAssignment> phoneAssignmentHistory
    ) {
        this.person = person;
        this.computerAssignmentHistory = List.copyOf(computerAssignmentHistory);
        this.phoneAssignmentHistory = List.copyOf(phoneAssignmentHistory);
        this.activeComputerAssignments = this.computerAssignmentHistory.stream()
                .filter(ComputerAssignment::isActive)
                .toList();
        this.activePhoneAssignments = this.phoneAssignmentHistory.stream()
                .filter(assignment -> assignment.getReturnedAt() == null)
                .toList();
    }

    public Person getPerson() {
        return person;
    }

    public List<ComputerAssignment> getComputerAssignmentHistory() {
        return computerAssignmentHistory;
    }

    public List<PhoneAssignment> getPhoneAssignmentHistory() {
        return phoneAssignmentHistory;
    }

    public List<ComputerAssignment> getActiveComputerAssignments() {
        return activeComputerAssignments;
    }

    public List<PhoneAssignment> getActivePhoneAssignments() {
        return activePhoneAssignments;
    }
}
