package com.companio.dto;

import com.companio.model.SosEvent;
import java.time.LocalDateTime;

public class SosEventResponse {

    private Long id;
    private Double latitude;
    private Double longitude;
    private String status;
    private LocalDateTime triggeredAt;
    private LocalDateTime alertAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime resolvedAt;

    public SosEventResponse(SosEvent event) {
        this.id = event.getId();
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
        this.status = event.getStatus().name();
        this.triggeredAt = event.getTriggeredAt();
        this.alertAt = event.getAlertAt();
        this.cancelledAt = event.getCancelledAt();
        this.resolvedAt = event.getResolvedAt();
    }

    public Long getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public LocalDateTime getAlertAt() {
        return alertAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}