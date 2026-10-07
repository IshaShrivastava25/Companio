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

    private LocalDateTime resolvedAt;

    public SosEvent() {
    }

    public SosEvent(User user, Double latitude, Double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = SosStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public SosStatus getStatus() { return status; }
    public void setStatus(SosStatus status) { this.status = status; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
