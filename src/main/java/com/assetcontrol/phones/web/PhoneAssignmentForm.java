package com.assetcontrol.phones.web;

import com.assetcontrol.phones.application.CreatePhoneAssignmentCommand;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PhoneAssignmentForm {

    private Long personId;

    @Size(max = 100, message = "El ID no puede superar 100 caracteres.")
    private String newPersonExternalId;

    @Size(max = 100, message = "El nombre de usuario no puede superar 100 caracteres.")
    private String newPersonUsername;

    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String newPersonFullName;

    @Email(message = "Escribe un correo válido.")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres.")
    private String newPersonEmail;

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
                newPersonExternalId,
                newPersonUsername,
                newPersonFullName,
                newPersonEmail,
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

    public boolean hasData() {
        return personId != null
                || hasNewPersonData()
                || dueAt != null
                || hasText(observations);
    }

    public boolean hasNewPersonData() {
        return hasText(newPersonExternalId)
                || hasText(newPersonUsername)
                || hasText(newPersonFullName)
                || hasText(newPersonEmail);
    }

    public boolean hasCompleteNewPersonData() {
        return hasText(newPersonExternalId)
                && hasText(newPersonUsername)
                && hasText(newPersonFullName)
                && hasText(newPersonEmail);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public String getNewPersonExternalId() {
        return newPersonExternalId;
    }

    public void setNewPersonExternalId(String newPersonExternalId) {
        this.newPersonExternalId = newPersonExternalId;
    }

    public String getNewPersonUsername() {
        return newPersonUsername;
    }

    public void setNewPersonUsername(String newPersonUsername) {
        this.newPersonUsername = newPersonUsername;
    }

    public String getNewPersonFullName() {
        return newPersonFullName;
    }

    public void setNewPersonFullName(String newPersonFullName) {
        this.newPersonFullName = newPersonFullName;
    }

    public String getNewPersonEmail() {
        return newPersonEmail;
    }

    public void setNewPersonEmail(String newPersonEmail) {
        this.newPersonEmail = newPersonEmail;
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
