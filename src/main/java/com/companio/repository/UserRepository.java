package com.companio.repository;

import com.companio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
/**
 * Repository for user-account persistence.
 */


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
