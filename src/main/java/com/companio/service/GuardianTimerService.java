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

    public GuardianTimer startTimer(String userEmail, int durationMinutes) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GuardianTimer timer = new GuardianTimer(user, durationMinutes);
        return guardianTimerRepository.save(timer);
    }

    public GuardianTimer checkIn(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GuardianTimer timer = guardianTimerRepository
                .findTopByUser_IdAndStatusOrderByStartedAtDesc(user.getId(), GuardianTimerStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active timer found"));

        timer.setStatus(GuardianTimerStatus.CHECKED_IN);
        return guardianTimerRepository.save(timer);
    }

    private static final long GRACE_MINUTES = 5;

    @Scheduled(fixedRate = 60000)
    public void checkExpiredTimers() {
        LocalDateTime now = LocalDateTime.now();
        List<GuardianTimer> expiredTimers = guardianTimerRepository
                .findByStatusAndExpiresAtBefore(GuardianTimerStatus.ACTIVE, now);

        for (GuardianTimer timer : expiredTimers) {
            String email = timer.getUser().getEmail();
            timer.setStatus(GuardianTimerStatus.EXPIRED);
            guardianTimerRepository.save(timer);

            if (timer.getExpiresAt().isBefore(now.minusMinutes(GRACE_MINUTES))) {
                System.out.println("Guardian Timer for " + email + " expired more than "
                        + GRACE_MINUTES + " minutes ago (server likely down) - SOS NOT auto-triggered.");
                continue;
            }

            sosEventService.triggerSos(email, null, null);
            System.out.println("Guardian Timer expired for user: " + email + " - SOS auto-triggered.");
        }
    }
}