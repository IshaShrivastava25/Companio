package com.companio.dto;

import java.time.LocalDateTime;
import java.util.List;

/** What a trusted contact sees when they open a tracking link. Deliberately minimal. */
public record TrackingResponse(
        String status,
        String name,
        Double latitude,
        Double longitude,
        LocalDateTime lastUpdated,
        List<TrackingPoint> recentPoints) {
}
