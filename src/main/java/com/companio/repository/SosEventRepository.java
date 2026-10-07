package com.companio.repository;

import com.companio.model.SosEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SosEventRepository extends JpaRepository<SosEvent, Long> {
    List<SosEvent> findByUser_Id(Long userId);
}
