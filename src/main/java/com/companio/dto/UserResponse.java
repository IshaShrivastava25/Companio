package com.companio.dto;

import com.companio.model.User;
/**
 * Response payload for a user profile.
 */


public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}