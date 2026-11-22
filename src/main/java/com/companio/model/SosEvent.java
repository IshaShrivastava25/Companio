package com.companio.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single SOS event and its lifecycle.
 */
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

    @Column(unique = true, length = 36)
    private String trackingToken;

    @Enumerated(EnumType.STRING)
    private SosStatus status;

    @CreationTimestamp
    private LocalDateTime triggeredAt;

    private LocalDateTime alertAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime resolvedAt;

    public static SosEvent activeNow(User user, Double latitude, Double longitude) {
        SosEvent event = new SosEvent();
        event.user = user;
        event.latitude = latitude;
        event.longitude = longitude;
        event.status = SosStatus.ACTIVE;
        event.alertAt = LocalDateTime.now();
        event.trackingToken = UUID.randomUUID().toString();
        return event;
    }

    public SosEvent() {
    }

    public SosEvent(User user, Double latitude, Double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = SosStatus.PENDING;
        this.triggeredAt = LocalDateTime.now();
        this.alertAt = LocalDateTime.now().plusSeconds(15);
        this.trackingToken = UUID.randomUUID().toString();
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

    public String getTrackingToken() {
        return trackingToken;
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