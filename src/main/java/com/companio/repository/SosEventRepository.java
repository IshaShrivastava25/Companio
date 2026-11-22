package com.companio.repository;

import com.companio.model.SosEvent;
import com.companio.model.SosStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SOS event records.
 */
public interface SosEventRepository extends JpaRepository<SosEvent, Long> {

    List<SosEvent> findByUser_Id(Long userId);

    // SOS events still in the confirmation window whose alert time has passed (5-second scheduler)
    List<SosEvent> findByStatusAndAlertAtBefore(SosStatus status, LocalDateTime alertAt);

    // Used by the public tracking link
    Optional<SosEvent> findByTrackingToken(String trackingToken);
}
