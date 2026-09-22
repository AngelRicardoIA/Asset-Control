package com.assetcontrol.accessories.domain;

import com.assetcontrol.people.domain.Person;
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
@Table(name = "accessory_assignments")
public class AccessoryAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "accessory_id", nullable = false)
    private Accessory accessory;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt;
    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;
    @Column(name = "returned_at")
    private LocalDate returnedAt;
    @Column(name = "received_by", length = 100)
    private String receivedBy;
    @Column(name = "return_notes", length = 1000)
    private String returnNotes;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AccessoryAssignment() {}
    public AccessoryAssignment(Accessory accessory, Person person, LocalDate assignedAt, String assignedBy) {
        this.accessory = accessory;
        this.person = person;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
    }
    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
    public void returnToStock(LocalDate date, String receiver, String notes) {
        if (returnedAt != null) throw new IllegalStateException("Este accesorio ya fue devuelto.");
        if (date.isBefore(assignedAt)) throw new IllegalArgumentException("La devolución no puede ser anterior a la asignación.");
        returnedAt = date;
        receivedBy = receiver;
        returnNotes = notes;
    }
    public Long getId() { return id; }
    public Accessory getAccessory() { return accessory; }
    public Person getPerson() { return person; }
    public LocalDate getAssignedAt() { return assignedAt; }
    public String getAssignedBy() { return assignedBy; }
    public LocalDate getReturnedAt() { return returnedAt; }
    public String getReceivedBy() { return receivedBy; }
    public String getReturnNotes() { return returnNotes; }
    public boolean isActive() { return returnedAt == null; }
}
