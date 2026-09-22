package com.assetcontrol.shared.web;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AssetReturnForm {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnedAt;

    private String receivedBy;
    private String returnNotes;

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getReturnNotes() {
        return returnNotes;
    }

    public void setReturnNotes(String returnNotes) {
        this.returnNotes = returnNotes;
    }
}
