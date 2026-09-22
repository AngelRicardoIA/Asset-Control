package com.assetcontrol.maintenance.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class MaintenanceScheduleForm {
    @NotNull
    private LocalDate nextDueAt;
    @NotBlank @Size(max = 500)
    private String reason;
    @NotBlank @Size(max = 100)
    private String recordedBy;
    public LocalDate getNextDueAt() { return nextDueAt; }
    public void setNextDueAt(LocalDate value) { nextDueAt = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String value) { recordedBy = value; }
}
