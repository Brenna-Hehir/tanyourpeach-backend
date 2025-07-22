package com.tanyourpeach.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_history_id")
    private Integer statusHistoryId;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private String status;

    // Optional: Either set by logged-in user or use client email
    @ManyToOne
    @JoinColumn(name = "changed_by_user_id")
    private User changedByUser;

    @Column(name = "changed_by_email")
    private String changedByEmail;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    // Getters and setters
    
    public Integer getStatusHistoryId() {
        return statusHistoryId;
    }

    public void setStatusHistoryId(Integer statusHistoryId) {
        this.statusHistoryId = statusHistoryId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
    }

    public String getchangedByEmail() {
        return changedByEmail;
    }

    public void setchangedByEmail(String changedByEmail) {
        this.changedByEmail = changedByEmail;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}