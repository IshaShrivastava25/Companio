package com.companio.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
/**
 * Represents a user safety timer and its state.
 */


@Entity
@Table(name = "guardian_timers")
public class GuardianTimer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime startedAt;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private GuardianTimerStatus status;

    public GuardianTimer() {
    }

    public GuardianTimer(User user, int durationMinutes) {
        this.user = user;
        this.expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);
        this.status = GuardianTimerStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public GuardianTimerStatus getStatus() { return status; }
    public void setStatus(GuardianTimerStatus status) { this.status = status; }
}