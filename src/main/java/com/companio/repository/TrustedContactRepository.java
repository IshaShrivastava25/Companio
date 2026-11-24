package com.companio.repository;

import com.companio.model.TrustedContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Repository for trusted-contact records.
 */

import java.util.Optional;
public interface TrustedContactRepository extends JpaRepository<TrustedContact, Long> {
    List<TrustedContact> findByUser_Id(Long userId);
    Optional<TrustedContact> findByConnectCode(String connectCode);
}
