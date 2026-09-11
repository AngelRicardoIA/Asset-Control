package com.assetcontrol.computers.domain;

import com.assetcontrol.sites.domain.Site;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "computers")
public class Computer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String asset;

    @Column(nullable = false, unique = true, length = 100)
    private String host;

    @Enumerated(EnumType.STRING)
    @Column(name = "computer_type", nullable = false, length = 30)
    private ComputerType type;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 150)
    private String model;

    @Column(name = "serial_number", nullable = false, length = 150)
    private String serialNumber;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComputerStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(length = 1000)
    private String observations;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Computer() {
    }

    public Computer(
            String asset,
            String host,
            ComputerType type,
            String brand,
            String model,
            String serialNumber,
            String operatingSystem,
            ComputerStatus status,
            Site site,
            String observations
    ) {
        this.asset = asset;
        this.host = host;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.operatingSystem = operatingSystem;
        this.status = status;
        this.site = site;
        this.observations = observations;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAsset() {
        return asset;
    }

    public String getHost() {
        return host;
    }

    public ComputerType getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public ComputerStatus getStatus() {
        return status;
    }

    public Site getSite() {
        return site;
    }

    public String getObservations() {
        return observations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}