package com.assetcontrol.assignments.domain;

import com.assetcontrol.computers.domain.Computer;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "computer_assignments")
public class ComputerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 30)
    private AssignmentType assignmentType;

    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ComputerAssignment() {
    }

    public ComputerAssignment(
            Computer computer,
            Person person,
            AssignmentType assignmentType,
            LocalDate assignedAt,
            LocalDate dueDate,
            String notes
    ) {
        this.computer = computer;
        this.person = person;
        this.assignmentType = assignmentType;
        this.assignedAt = assignedAt;
        this.dueDate = dueDate;
        this.notes = notes;
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

    public boolean isActive() {
        return returnedAt == null;
    }

    public Long getId() {
        return id;
    }

    public Computer getComputer() {
        return computer;
    }

    public Person getPerson() {
        return person;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public String getNotes() {
        return notes;
    }
}