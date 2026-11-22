package com.companio.controller;

import com.companio.dto.LocationRequest;
import com.companio.dto.SosEventResponse;
import com.companio.dto.TrackingPoint;
import com.companio.model.LocationUpdate;
import com.companio.model.SosEvent;
import com.companio.service.SosEventService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exposes SOS lifecycle and location update endpoints.
 */
@RestController
@RequestMapping("/api/sos")
public class SosEventController {

    @Autowired
    private SosEventService sosEventService;

    // Trigger a new SOS with a 15-second confirmation window.
    @PostMapping("/trigger")
    public ResponseEntity<SosEventResponse> triggerSos(
            Authentication authentication,
            @RequestBody Map<String, Double> body) {

        SosEvent event = sosEventService.triggerSos(
                authentication.getName(),
                body.get("latitude"),
                body.get("longitude")
        );
        return ResponseEntity.ok(new SosEventResponse(event));
    }

    // Retrieve the authenticated user's SOS events.
    @GetMapping
    public ResponseEntity<List<SosEventResponse>> getMyEvents(Authentication authentication) {
        List<SosEventResponse> responses = sosEventService.getMyEvents(authentication.getName())
                .stream()
                .map(SosEventResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Cancel a pending SOS using the real PIN or duress PIN.
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SosEventResponse> cancelSos(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        SosEvent event = sosEventService.cancelSos(
                authentication.getName(),
                id,
                body.get("pin")
        );
        return ResponseEntity.ok(new SosEventResponse(event));
    }

    // Resolve an active SOS event.
    @PutMapping("/{id}/resolve")
    public ResponseEntity<SosEventResponse> resolveSos(
            Authentication authentication,
            @PathVariable Long id) {

        SosEvent event = sosEventService.resolveSos(authentication.getName(), id);
        return ResponseEntity.ok(new SosEventResponse(event));
    }

    // Record a new location update for an ongoing SOS event.
    @PostMapping("/{id}/location")
    public ResponseEntity<TrackingPoint> updateLocation(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest body) {

        LocationUpdate saved = sosEventService.recordLocation(
                authentication.getName(),
                id,
                body.latitude(),
                body.longitude(),
                body.accuracyMeters()
        );
        return ResponseEntity.ok(
                new TrackingPoint(saved.getLatitude(), saved.getLongitude(), saved.getRecordedAt())
        );
    }
}