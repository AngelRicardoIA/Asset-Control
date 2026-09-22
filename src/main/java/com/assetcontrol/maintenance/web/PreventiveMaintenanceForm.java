package com.assetcontrol.maintenance.web;

import com.assetcontrol.maintenance.application.CreatePreventiveMaintenanceCommand;
import com.assetcontrol.maintenance.domain.MaintenanceCheck;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PreventiveMaintenanceForm {
    @NotNull
    @PastOrPresent
    private LocalDate performedAt = LocalDate.now();
    @NotBlank
    @Size(max = 100)
    private String performedBy;
    @NotBlank
    @Size(max = 1000)
    private String description;
    @NotNull
    private MaintenanceCheck updatesCheck;
    @NotNull
    private MaintenanceCheck driversCheck;
    @NotNull
    private MaintenanceCheck externalCleaning;
    @NotNull
    private MaintenanceCheck internalCleaning;
    @Size(max = 1000)
    private String checklistNotes;

    @AssertTrue(message = "Explica las comprobaciones marcadas como no aplica.")
    public boolean isChecklistExplained() {
        return (updatesCheck != MaintenanceCheck.NOT_APPLICABLE && driversCheck != MaintenanceCheck.NOT_APPLICABLE
                && externalCleaning != MaintenanceCheck.NOT_APPLICABLE && internalCleaning != MaintenanceCheck.NOT_APPLICABLE)
                || (checklistNotes != null && !checklistNotes.isBlank());
    }

    public CreatePreventiveMaintenanceCommand toCommand(Long computerId) {
        return new CreatePreventiveMaintenanceCommand(computerId, performedAt, performedBy, description,
                updatesCheck, driversCheck, externalCleaning, internalCleaning, checklistNotes);
    }
    public LocalDate getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDate value) { performedAt = value; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String value) { performedBy = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public MaintenanceCheck getUpdatesCheck() { return updatesCheck; }
    public void setUpdatesCheck(MaintenanceCheck value) { updatesCheck = value; }
    public MaintenanceCheck getDriversCheck() { return driversCheck; }
    public void setDriversCheck(MaintenanceCheck value) { driversCheck = value; }
    public MaintenanceCheck getExternalCleaning() { return externalCleaning; }
    public void setExternalCleaning(MaintenanceCheck value) { externalCleaning = value; }
    public MaintenanceCheck getInternalCleaning() { return internalCleaning; }
    public void setInternalCleaning(MaintenanceCheck value) { internalCleaning = value; }
    public String getChecklistNotes() { return checklistNotes; }
    public void setChecklistNotes(String value) { checklistNotes = value; }
}
