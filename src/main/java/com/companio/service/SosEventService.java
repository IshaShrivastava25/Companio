package com.companio.service;

import com.companio.model.SosEvent;
import com.companio.model.SosStatus;
import com.companio.model.User;
import com.companio.repository.SosEventRepository;
import com.companio.repository.UserRepository;
import com.companio.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SosEventService {

    private static final int CONFIRMATION_WINDOW_SECONDS = 15;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SosEventRepository sosEventRepository;

    @Autowired
    private UserRepository userRepository;

    // Safely attempt to notify trusted contacts.
    private void notifyContactsSafely(SosEvent event) {
        try {
            notificationService.notifyContacts(event);
        } catch (Exception e) {
            System.err.println(
                    "Failed to notify contacts for SOS "
                            + event.getId() + ": " + e.getMessage()
            );
        }
    }

    // Find an SOS event and verify that it belongs to the authenticated user.
    private SosEvent findOwnedEvent(String userEmail, Long eventId) {
        return sosEventRepository.findById(eventId)
                .filter(event -> event.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("SOS event not found"));
    }

    // Trigger a new SOS with a 15-second cancellation window.
    public SosEvent triggerSos(
            String userEmail,
            Double latitude,
            Double longitude) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SosEvent event = new SosEvent(user, latitude, longitude);

        return sosEventRepository.save(event);
    }

    // Trigger an SOS that becomes ACTIVE immediately.
    public SosEvent triggerSosImmediately(
            String userEmail,
            Double latitude,
            Double longitude) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SosEvent event = sosEventRepository.save(
                SosEvent.activeNow(user, latitude, longitude)
        );

        notifyContactsSafely(event);

        System.out.println(
                "SOS " + event.getId()
                        + " is ACTIVE immediately (no confirmation window)."
        );

        return event;
    }

    // Return the authenticated user's SOS events.
    public List<SosEvent> getMyEvents(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sosEventRepository.findByUser_Id(user.getId());
    }

    // Cancel a pending SOS using either the real PIN or duress PIN.
    @Transactional
    public SosEvent cancelSos(
            String userEmail,
            Long eventId,
            String pin) {

        SosEvent event = findOwnedEvent(userEmail, eventId);
        User user = event.getUser();

        // Only pending SOS events can be cancelled.
        if (event.getStatus() != SosStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a PENDING SOS can be cancelled"
            );
        }

        // Preserve the 15-second cancellation deadline.
        if (event.getAlertAt() == null
                || !LocalDateTime.now().isBefore(event.getAlertAt())) {
            throw new IllegalStateException(
                    "The SOS confirmation window has expired"
            );
        }

        // Both PINs must be configured before cancellation.
        if (user.getPinHash() == null
                || user.getDuressPinHash() == null) {
            throw new IllegalStateException(
                    "Set your PINs before cancelling an SOS"
            );
        }

        // Validate the submitted PIN format.
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException(
                    "A valid 4-to-6-digit PIN is required"
            );
        }

        // Real PIN: cancel the SOS.
        if (passwordEncoder.matches(pin, user.getPinHash())) {
            event.setStatus(SosStatus.CANCELLED);
            event.setCancelledAt(LocalDateTime.now());

            return sosEventRepository.save(event);
        }

        // Duress PIN: leave the SOS pending so it activates at the deadline.
        if (passwordEncoder.matches(pin, user.getDuressPinHash())) {
            System.out.println(
                    "[DURESS] SOS " + event.getId()
                            + ": duress PIN used. SOS remains PENDING."
            );

            return event;
        }

        // Neither PIN matched.
        throw new IllegalArgumentException("Incorrect PIN");
    }

    // Resolve an active SOS event.
    @Transactional
    public SosEvent resolveSos(String userEmail, Long eventId) {
        SosEvent event = findOwnedEvent(userEmail, eventId);

        if (event.getStatus() != SosStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active SOS events can be resolved"
            );
        }

        event.setStatus(SosStatus.RESOLVED);
        event.setResolvedAt(LocalDateTime.now());

        return sosEventRepository.save(event);
    }

    // Activate pending SOS events after their confirmation windows expire.
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void activatePendingSosEvents() {
        LocalDateTime now = LocalDateTime.now();

        List<SosEvent> pendingEvents =
                sosEventRepository.findByStatusAndAlertAtBefore(
                        SosStatus.PENDING,
                        now
                );

        for (SosEvent event : pendingEvents) {
            if (event.getStatus() != SosStatus.PENDING) {
                continue;
            }

            event.setStatus(SosStatus.ACTIVE);
            sosEventRepository.save(event);

            notifyContactsSafely(event);

            System.out.println(
                    "SOS event ID " + event.getId()
                            + " is now ACTIVE."
            );
        }
    }
}