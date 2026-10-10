package com.companio.controller;

import com.companio.dto.SosEventResponse;
import com.companio.model.SosEvent;
import com.companio.service.SosEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        String userEmail = authentication.getName();

        SosEvent event = sosEventService.triggerSos(
                userEmail,
                body.get("latitude"),
                body.get("longitude")
        );

        return ResponseEntity.ok(new SosEventResponse(event));
    }

    // Retrieve the authenticated user's SOS events.
    @GetMapping
    public ResponseEntity<List<SosEventResponse>> getMyEvents(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<SosEventResponse> responses =
                sosEventService.getMyEvents(userEmail)
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

        String userEmail = authentication.getName();
        String pin = body.get("pin");

        SosEvent event = sosEventService.cancelSos(
                userEmail,
                id,
                pin
        );

        return ResponseEntity.ok(new SosEventResponse(event));
    }

    // Resolve an active SOS event.
    @PutMapping("/{id}/resolve")
    public ResponseEntity<SosEventResponse> resolveSos(
            Authentication authentication,
            @PathVariable Long id) {

        String userEmail = authentication.getName();

        SosEvent event = sosEventService.resolveSos(
                userEmail,
                id
        );

        return ResponseEntity.ok(new SosEventResponse(event));
    }
}