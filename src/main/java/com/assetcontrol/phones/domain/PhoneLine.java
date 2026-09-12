package com.assetcontrol.phones.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_lines")
public class PhoneLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String number;

    @Column(length = 100)
    private String carrier;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PhoneLine() {
    }

    public PhoneLine(String number, String carrier) {
        this.number = number;
        this.carrier = carrier;
    }

    public void updateDetails(String number, String carrier) {
        this.number = number;
        this.carrier = carrier;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getCarrier() {
        return carrier;
    }
}