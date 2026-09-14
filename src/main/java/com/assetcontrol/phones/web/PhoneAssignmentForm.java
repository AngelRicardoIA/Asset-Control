package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.CreatePhoneAssignmentCommand;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PhoneAssignmentForm {

    @NotNull(message = "Selecciona una persona.")
    private Long personId;

    @NotNull(message = "Selecciona el tipo de asignación.")
    private PhoneAssignmentType type = PhoneAssignmentType.ASSIGNMENT;

    @NotNull(message = "Indica la fecha de asignación.")
    private LocalDate assignedAt = LocalDate.now();

    private LocalDate dueAt;

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres.")
    private String observations;

    public CreatePhoneAssignmentCommand toCommand() {
        return new CreatePhoneAssignmentCommand(
                personId,
                type,
                assignedAt,
                dueAt,
                observations
        );
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public PhoneAssignmentType getType() {
        return type;
    }

    public void setType(PhoneAssignmentType type) {
        this.type = type;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDate dueAt) {
        this.dueAt = dueAt;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}