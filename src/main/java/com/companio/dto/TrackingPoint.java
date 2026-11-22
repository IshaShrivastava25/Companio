package com.companio.dto;

import java.time.LocalDateTime;

public record TrackingPoint(Double latitude, Double longitude, LocalDateTime recordedAt) {
}
