package com.companio.repository;

import com.companio.model.TrustedContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrustedContactRepository extends JpaRepository<TrustedContact, Long> {
    List<TrustedContact> findByUser_Id(Long userId);
}
