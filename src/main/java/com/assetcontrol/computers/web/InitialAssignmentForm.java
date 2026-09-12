package com.assetcontrol.computers.web;

import com.assetcontrol.assignments.application.CreateComputerAssignmentCommand;
import com.assetcontrol.assignments.domain.AssignmentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class InitialAssignmentForm {

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

    private AssignmentType assignmentType;

    private LocalDate assignedAt = LocalDate.now();

    private LocalDate dueDate;

    @Size(max = 1000, message = "Las notas no pueden superar 1000 caracteres.")
    private String notes;

    public boolean hasData() {
        return personId != null
                || hasText(newPersonExternalId)
                || hasText(newPersonUsername)
                || hasText(newPersonFullName)
                || hasText(newPersonEmail)
                || assignmentType != null
                || dueDate != null
                || hasText(notes);
    }

    public boolean hasCompleteNewPersonData() {
        return hasText(newPersonExternalId)
                && hasText(newPersonUsername)
                && hasText(newPersonFullName)
                && hasText(newPersonEmail);
    }

    public CreateComputerAssignmentCommand toCommand(Long computerId) {
        return new CreateComputerAssignmentCommand(
                computerId,
                personId,
                newPersonExternalId,
                newPersonUsername,
                newPersonFullName,
                newPersonEmail,
                assignmentType,
                assignedAt,
                dueDate,
                notes
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
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

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}