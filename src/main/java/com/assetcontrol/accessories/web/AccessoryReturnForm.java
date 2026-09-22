package com.assetcontrol.accessories.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AccessoryReturnForm {
    @NotNull @PastOrPresent private LocalDate returnedAt = LocalDate.now();
    @NotBlank @Size(max = 100) private String receivedBy;
    @Size(max = 1000) private String notes;
    public LocalDate getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDate value) { returnedAt = value; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String value) { receivedBy = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
}
