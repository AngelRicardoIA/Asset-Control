package com.assetcontrol.accessories.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AccessoryAssignForm {
    @NotNull private Long personId;
    @NotNull @PastOrPresent private LocalDate assignedAt = LocalDate.now();
    @NotBlank @Size(max = 100) private String assignedBy;
    public Long getPersonId() { return personId; }
    public void setPersonId(Long value) { personId = value; }
    public LocalDate getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDate value) { assignedAt = value; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String value) { assignedBy = value; }
}
