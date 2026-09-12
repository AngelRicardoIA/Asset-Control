package com.assetcontrol.maintenance.domain;

import com.assetcontrol.computers.domain.Computer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "computer_maintenance_records")
public class ComputerMaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 30)
    private ComputerMaintenanceType recordType;

    @Column(name = "performed_at", nullable = false)
    private LocalDate performedAt;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ComputerMaintenanceRecord() {
    }

    public ComputerMaintenanceRecord(
            Computer computer,
            ComputerMaintenanceType recordType,
            LocalDate performedAt,
            String description,
            String performedBy
    ) {
        this.computer = computer;
        this.recordType = recordType;
        this.performedAt = performedAt;
        this.description = description;
        this.performedBy = performedBy;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ComputerMaintenanceType getRecordType() {
        return recordType;
    }

    public LocalDate getPerformedAt() {
        return performedAt;
    }

    public String getDescription() {
        return description;
    }

    public String getPerformedBy() {
        return performedBy;
    }
}