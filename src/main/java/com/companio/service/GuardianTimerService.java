package com.companio.service;

import com.companio.model.GuardianTimer;
import com.companio.model.GuardianTimerStatus;
import com.companio.model.User;
import com.companio.repository.GuardianTimerRepository;
import com.companio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GuardianTimerService {

    @Autowired
    private GuardianTimerRepository guardianTimerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SosEventService sosEventService;

    private static final long GRACE_MINUTES = 5;

    public GuardianTimer startTimer(String userEmail, int durationMinutes) {

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Timer duration must be greater than zero"
            );
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GuardianTimer timer = new GuardianTimer(user, durationMinutes);

        GuardianTimer savedTimer = guardianTimerRepository.save(timer);

        System.out.println(
                "Guardian Timer started. ID: " + savedTimer.getId()
                        + ", expires at: " + savedTimer.getExpiresAt()
        );

        return savedTimer;
    }

    public GuardianTimer checkIn(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GuardianTimer timer = guardianTimerRepository
                .findTopByUser_IdAndStatusOrderByStartedAtDesc(
                        user.getId(),
                        GuardianTimerStatus.ACTIVE
                )
                .orElseThrow(() -> new RuntimeException("No active timer found"));

        timer.setStatus(GuardianTimerStatus.CHECKED_IN);

        GuardianTimer savedTimer = guardianTimerRepository.save(timer);

        System.out.println(
                "Guardian Timer checked in. ID: " + savedTimer.getId()
        );

        return savedTimer;
    }

    @Scheduled(fixedRate = 60000)
    public void checkExpiredTimers() {
        LocalDateTime now = LocalDateTime.now();
        List<GuardianTimer> expiredTimers = guardianTimerRepository
                .findByStatusAndExpiresAtBefore(GuardianTimerStatus.ACTIVE, now);

        for (GuardianTimer timer : expiredTimers) {
            String email = timer.getUser().getEmail();
            try {
                if (timer.getExpiresAt().isBefore(now.minusMinutes(GRACE_MINUTES))) {
                    timer.setStatus(GuardianTimerStatus.EXPIRED);
                    guardianTimerRepository.save(timer);
                    System.out.println("Timer " + timer.getId() + " expired over " + GRACE_MINUTES
                            + " minutes ago - SOS NOT auto-triggered.");
                    continue;
                }

                sosEventService.triggerSos(email, null, null);   // fire the SOS first
                timer.setStatus(GuardianTimerStatus.EXPIRED);    // only then mark it done
                guardianTimerRepository.save(timer);
                System.out.println("Guardian Timer expired for user: " + email + " - SOS auto-triggered.");
            } catch (Exception e) {
                System.err.println("Failed to process timer " + timer.getId() + ": " + e.getMessage()
                        + " - will retry next cycle");
            }
        }
    }
}

