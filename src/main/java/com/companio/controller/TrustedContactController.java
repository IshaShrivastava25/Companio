package com.companio.controller;

import com.companio.dto.ContactRequest;
import com.companio.dto.TrustedContactResponse;
import com.companio.model.TrustedContact;
import com.companio.service.TrustedContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages trusted-contact CRUD requests.
 */
@RestController
@RequestMapping("/api/contacts")
public class TrustedContactController {

    @Autowired
    private TrustedContactService trustedContactService;

    @Value("${telegram.bot-username:}")
    private String botUsername;

    @PostMapping
    public ResponseEntity<TrustedContactResponse> addContact(
            Authentication authentication,
            @Valid @RequestBody ContactRequest body) {

        TrustedContact contact = trustedContactService.addContact(
                authentication.getName(),
                body.name(),
                body.phoneNumber(),
                body.relationship()
        );
        return ResponseEntity.ok(new TrustedContactResponse(contact, botUsername));
    }

    @GetMapping
    public ResponseEntity<List<TrustedContactResponse>> getMyContacts(Authentication authentication) {
        List<TrustedContactResponse> responses = trustedContactService
                .getContactsForUser(authentication.getName())
                .stream()
                .map(c -> new TrustedContactResponse(c, botUsername))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrustedContactResponse> updateContact(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest body) {

        TrustedContact contact = trustedContactService.updateContact(
                authentication.getName(),
                id,
                body.name(),
                body.phoneNumber(),
                body.relationship()
        );
        return ResponseEntity.ok(new TrustedContactResponse(contact, botUsername));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            Authentication authentication,
            @PathVariable Long id) {

        trustedContactService.deleteContact(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}