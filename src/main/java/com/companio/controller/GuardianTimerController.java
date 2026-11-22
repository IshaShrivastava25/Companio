package com.companio.controller;

import com.companio.dto.GuardianTimerResponse;
import com.companio.model.GuardianTimer;
import com.companio.service.GuardianTimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * Exposes guardian-timer lifecycle endpoints.
 */


@RestController
@RequestMapping("/api/guardian-timer")
public class GuardianTimerController {

    @Autowired
    private GuardianTimerService guardianTimerService;

    @PostMapping("/start")
    public ResponseEntity<GuardianTimerResponse> startTimer(
            Authentication authentication,
            @RequestBody Map<String, Integer> body) {

        GuardianTimer timer = guardianTimerService.startTimer(authentication.getName(), body.get("durationMinutes"));
        return ResponseEntity.ok(new GuardianTimerResponse(timer));
    }

    @PutMapping("/check-in")
    public ResponseEntity<GuardianTimerResponse> checkIn(Authentication authentication) {
        GuardianTimer timer = guardianTimerService.checkIn(authentication.getName());
        return ResponseEntity.ok(new GuardianTimerResponse(timer));
    }
}