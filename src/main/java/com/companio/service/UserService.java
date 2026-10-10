package com.companio.service;

import com.companio.model.User;
import com.companio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void setPins(String email, String pin, String duressPin) {
        if (pin == null || !pin.matches("\\d{4,6}")
                || duressPin == null || !duressPin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PINs must be 4 to 6 digits");
        }

        if (pin.equals(duressPin)) {
            throw new IllegalArgumentException(
                    "Duress PIN must differ from the real PIN"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPinHash(passwordEncoder.encode(pin));
        user.setDuressPinHash(passwordEncoder.encode(duressPin));

        userRepository.save(user);
    }
}