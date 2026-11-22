package com.companio.repository;

import com.companio.model.LocationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationUpdateRepository extends JpaRepository<LocationUpdate, Long> {

    // The 20 most recent points for an SOS, newest first.
    List<LocationUpdate> findTop20BySosEvent_IdOrderByRecordedAtDesc(Long sosEventId);
}
