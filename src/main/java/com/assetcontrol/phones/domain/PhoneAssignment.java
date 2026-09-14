package com.assetcontrol.phones.domain;

import com.assetcontrol.people.domain.Person;
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
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phone_assignments")
public class PhoneAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_id", nullable = false)
    private Phone phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 30)
    private PhoneAssignmentType type;

    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt;

    @Column(name = "due_at")
    private LocalDate dueAt;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Column(length = 2000)
    private String observations;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PhoneAssignment() {
    }

    public PhoneAssignment(
            Phone phone,
            Person person,
            PhoneAssignmentType type,
            LocalDate assignedAt,
            LocalDate dueAt,
            String observations
    ) {
        this.phone = phone;
        this.person = person;
        this.type = type;
        this.assignedAt = assignedAt;
        this.dueAt = dueAt;
        this.observations = observations;
    }

    public void returnOn(LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    public Long getId() {
        return id;
    }

    public Phone getPhone() {
        return phone;
    }

    public Person getPerson() {
        return person;
    }

    public PhoneAssignmentType getType() {
        return type;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public String getObservations() {
        return observations;
    }
}