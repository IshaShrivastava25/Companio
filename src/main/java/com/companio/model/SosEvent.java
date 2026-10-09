package com.companio.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sos_events")
public class SosEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private SosStatus status;

    @CreationTimestamp
    private LocalDateTime triggeredAt;

    private LocalDateTime alertAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime resolvedAt;

    public SosEvent() {
    }

    public SosEvent(User user, Double latitude, Double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = SosStatus.PENDING;
        this.triggeredAt = LocalDateTime.now();
        this.alertAt = LocalDateTime.now().plusSeconds(15);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public SosStatus getStatus() {
        return status;
    }

    public void setStatus(SosStatus status) {
        this.status = status;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public LocalDateTime getAlertAt() {
        return alertAt;
    }

    public void setAlertAt(LocalDateTime alertAt) {
        this.alertAt = alertAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}