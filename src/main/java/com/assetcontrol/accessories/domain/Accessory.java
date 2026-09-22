package com.assetcontrol.accessories.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "accessories")
public class Accessory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "accessory_type", nullable = false, length = 30)
    private AccessoryType type;
    @Column(nullable = false, length = 100)
    private String brand;
    @Column(nullable = false, length = 150)
    private String model;
    @Column(name = "serial_number", length = 150)
    private String serialNumber;
    @Column(length = 100)
    private String asset;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Accessory() {}
    public Accessory(AccessoryType type, String brand, String model, String serialNumber, String asset) {
        update(type, brand, model, serialNumber, asset);
    }
    public void update(AccessoryType type, String brand, String model, String serialNumber, String asset) {
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.asset = asset;
    }
    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public AccessoryType getType() { return type; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public String getAsset() { return asset; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
