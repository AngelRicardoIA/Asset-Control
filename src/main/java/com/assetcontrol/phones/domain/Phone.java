package com.assetcontrol.phones.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "phones")
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String imei;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PhoneStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_line_id")
    private PhoneLine phoneLine;

    @Column(length = 2000)
    private String observations;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Phone() {
    }

    public Phone(
            String imei,
            String brand,
            String model,
            PhoneStatus status,
            Site site,
            PhoneLine phoneLine,
            String observations
    ) {
        this.imei = imei;
        this.brand = brand;
        this.model = model;
        this.status = status;
        this.site = site;
        this.phoneLine = phoneLine;
        this.observations = observations;
    }

    public void updateDetails(
            String imei,
            String brand,
            String model,
            Site site,
            PhoneLine phoneLine,
            String observations
    ) {
        this.imei = imei;
        this.brand = brand;
        this.model = model;
        this.site = site;
        this.phoneLine = phoneLine;
        this.observations = observations;
    }

    public void changeStatus(PhoneStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getImei() {
        return imei;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public PhoneStatus getStatus() {
        return status;
    }

    public Site getSite() {
        return site;
    }

    public PhoneLine getPhoneLine() {
        return phoneLine;
    }

    public String getObservations() {
        return observations;
    }
}