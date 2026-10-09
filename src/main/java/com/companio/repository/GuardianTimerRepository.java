package com.companio.repository;

import com.companio.model.GuardianTimer;
import com.companio.model.GuardianTimerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GuardianTimerRepository extends JpaRepository<GuardianTimer, Long> {
    Optional<GuardianTimer> findTopByUser_IdAndStatusOrderByStartedAtDesc(Long userId, GuardianTimerStatus status);
    List<GuardianTimer> findByStatusAndExpiresAtBefore(GuardianTimerStatus status, LocalDateTime time);
}