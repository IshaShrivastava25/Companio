package com.companio.controller;

import com.companio.dto.UserResponse;
import com.companio.model.User;
import com.companio.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody User user) {

        User savedUser = userService.createUser(user);

        return ResponseEntity.ok(new UserResponse(savedUser));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> responses = userService.getAllUsers()
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/me/pins")
    public ResponseEntity<Void> setPins(
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        userService.setPins(
                authentication.getName(),
                body.get("pin"),
                body.get("duressPin")
        );

        return ResponseEntity.noContent().build();
    }
}