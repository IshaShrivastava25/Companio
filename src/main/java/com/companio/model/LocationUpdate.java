package com.companio.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_updates",
       indexes = @Index(name = "idx_location_event_time", columnList = "sos_event_id, recorded_at"))
public class LocationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sos_event_id", nullable = false)
    private SosEvent sosEvent;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double accuracyMeters;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public LocationUpdate() {
    }

    public LocationUpdate(SosEvent sosEvent, Double latitude, Double longitude, Double accuracyMeters) {
        this.sosEvent = sosEvent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyMeters = accuracyMeters;
    }

    public Long getId() { return id; }
    public SosEvent getSosEvent() { return sosEvent; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getAccuracyMeters() { return accuracyMeters; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
