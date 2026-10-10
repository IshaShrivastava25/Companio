package com.companio.service;

import com.companio.model.SosEvent;
import com.companio.model.SosStatus;
import com.companio.model.User;
import com.companio.repository.SosEventRepository;
import com.companio.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SosEventService {

    private static final int CONFIRMATION_WINDOW_SECONDS = 15;

    @Autowired
    private SosEventRepository sosEventRepository;

    @Autowired
    private UserRepository userRepository;

    // Trigger a new SOS event.
    public SosEvent triggerSos(
            String userEmail,
            Double latitude,
            Double longitude) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SosEvent event = new SosEvent(user, latitude, longitude);

        return sosEventRepository.save(event);
    }
    public SosEvent triggerSosImmediately(String userEmail, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SosEvent event = sosEventRepository.save(SosEvent.activeNow(user, latitude, longitude));
        System.out.println("SOS " + event.getId() + " is ACTIVE immediately (no confirmation window).");
        return event;
    }

    // Return the authenticated user's SOS events.
    public List<SosEvent> getMyEvents(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sosEventRepository.findByUser_Id(user.getId());
    }

    // Cancel a pending SOS during the confirmation window.
    @Transactional
    public SosEvent cancelSos(String userEmail, Long eventId) {

        SosEvent event = sosEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("SOS event not found"));

        if (!event.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException(
                    "Not authorized to cancel this event");
        }

        if (event.getStatus() != SosStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending SOS events can be cancelled");
        }

        if (event.getAlertAt() == null
                || !LocalDateTime.now().isBefore(event.getAlertAt())) {
            throw new IllegalStateException(
                    "The SOS confirmation window has expired");
        }

        event.setStatus(SosStatus.CANCELLED);
        event.setCancelledAt(LocalDateTime.now());

        return sosEventRepository.save(event);
    }

    // Resolve an active SOS event.
    @Transactional
    public SosEvent resolveSos(String userEmail, Long eventId) {

        SosEvent event = sosEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("SOS event not found"));

        if (!event.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException(
                    "Not authorized to resolve this event");
        }

        if (event.getStatus() != SosStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active SOS events can be resolved");
        }

        event.setStatus(SosStatus.RESOLVED);
        event.setResolvedAt(LocalDateTime.now());

        return sosEventRepository.save(event);
    }

    // Activate pending SOS events after their confirmation window expires.
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

            System.out.println(
                    "SOS event ID " + event.getId()
                            + " is now ACTIVE."
            );
        }
    }
}