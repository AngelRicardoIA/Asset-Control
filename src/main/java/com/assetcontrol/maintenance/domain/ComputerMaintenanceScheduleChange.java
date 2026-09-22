package com.assetcontrol.maintenance.domain;

import com.assetcontrol.computers.domain.Computer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "computer_maintenance_schedule_changes")
public class ComputerMaintenanceScheduleChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @Column(name = "next_due_at", nullable = false)
    private LocalDate nextDueAt;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "recorded_by", nullable = false, length = 100)
    private String recordedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ComputerMaintenanceScheduleChange() {
    }

    public ComputerMaintenanceScheduleChange(Computer computer, LocalDate nextDueAt, String reason, String recordedBy) {
        this.computer = computer;
        this.nextDueAt = nextDueAt;
        this.reason = reason;
        this.recordedBy = recordedBy;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Computer getComputer() {
        return computer;
    }

    public LocalDate getNextDueAt() {
        return nextDueAt;
    }

    public String getReason() {
        return reason;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
