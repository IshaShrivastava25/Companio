package com.companio.controller;

import com.companio.dto.TrackingResponse;
import com.companio.service.SosEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint for trusted contacts. It needs no login: the unguessable token in the link is the credential.
 * It only answers while the SOS is ACTIVE, so a cancelled or resolved SOS is no longer visible.
 */
@RestController
@RequestMapping("/api/track")
public class TrackingController {

    @Autowired
    private SosEventService sosEventService;

    @GetMapping("/{token}")
    public ResponseEntity<TrackingResponse> track(@PathVariable String token) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sosEventService.getTracking(token));
    }
}
