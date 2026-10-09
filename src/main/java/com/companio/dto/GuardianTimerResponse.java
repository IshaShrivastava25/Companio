package com.companio.dto;

import com.companio.model.GuardianTimer;
import java.time.LocalDateTime;

public class GuardianTimerResponse {

    private Long id;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;

    public GuardianTimerResponse(GuardianTimer timer) {
        this.id = timer.getId();
        this.status = timer.getStatus().name();
        this.startedAt = timer.getStartedAt();
        this.expiresAt = timer.getExpiresAt();
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}