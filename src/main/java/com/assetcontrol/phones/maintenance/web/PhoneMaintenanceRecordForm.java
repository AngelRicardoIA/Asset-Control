package com.assetcontrol.phones.maintenance.web;

import com.assetcontrol.phones.maintenance.application.CreatePhoneMaintenanceRecordCommand;
import com.assetcontrol.phones.maintenance.domain.PhoneMaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PhoneMaintenanceRecordForm {

    @NotNull(message = "Selecciona el tipo de registro.")
    private PhoneMaintenanceType recordType;

    @NotNull(message = "Selecciona la fecha.")
    private LocalDate performedAt = LocalDate.now();

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
    private String description;

    @NotBlank(message = "Indica quién realizó el trabajo.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String performedBy;

    public CreatePhoneMaintenanceRecordCommand toCommand(Long phoneId) {
        return new CreatePhoneMaintenanceRecordCommand(
                phoneId,
                recordType,
                performedAt,
                description,
                performedBy
        );
    }

    public PhoneMaintenanceType getRecordType() {
        return recordType;
    }

    public void setRecordType(PhoneMaintenanceType recordType) {
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
