package com.companio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name is too long") String name,
        @NotBlank(message = "Phone number is required") @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number must be 7 to 15 digits") String phoneNumber,
        @Size(max = 50, message = "Relationship is too long") String relationship) {
}
