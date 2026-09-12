package com.assetcontrol.maintenance.web;

import com.assetcontrol.maintenance.application.CreateComputerMaintenanceRecordCommand;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ComputerMaintenanceRecordForm {

    @NotNull(message = "Selecciona el tipo de registro.")
    private ComputerMaintenanceType recordType;

    @NotNull(message = "Selecciona la fecha.")
    private LocalDate performedAt = LocalDate.now();

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
    private String description;

    @NotBlank(message = "Indica quién realizó el trabajo.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String performedBy;

    public CreateComputerMaintenanceRecordCommand toCommand(Long computerId) {
        return new CreateComputerMaintenanceRecordCommand(
                computerId,
                recordType,
                performedAt,
                description,
                performedBy
        );
    }

    public ComputerMaintenanceType getRecordType() {
        return recordType;
    }

    public void setRecordType(ComputerMaintenanceType recordType) {
        this.recordType = recordType;
    }

    public LocalDate getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDate performedAt) {
        this.performedAt = performedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
}