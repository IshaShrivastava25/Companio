package com.companio.service;

import com.companio.exception.ResourceNotFoundException;
import com.companio.model.User;
import com.companio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
/**
 * Coordinates user registration, lookup, and PIN updates.
 */


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("That email is already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void setPins(String email, String pin, String duressPin) {
        if (pin == null || !pin.matches("\\d{4,6}")
                || duressPin == null || !duressPin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PINs must be 4 to 6 digits");
        }

        if (pin.equals(duressPin)) {
            throw new IllegalArgumentException("Duress PIN must differ from the real PIN");
        }

        User user = getByEmail(email);
        user.setPinHash(passwordEncoder.encode(pin));
        user.setDuressPinHash(passwordEncoder.encode(duressPin));
        userRepository.save(user);
    }
}