package com.companio.controller;

import com.companio.dto.RegisterRequest;
import com.companio.dto.UserResponse;
import com.companio.model.User;
import com.companio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest req) {
        User user = new User(req.name(), req.email(), req.password(), req.phoneNumber());
        return ResponseEntity.ok(new UserResponse(userService.createUser(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(new UserResponse(userService.getByEmail(authentication.getName())));
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