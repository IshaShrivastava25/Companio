package com.companio.controller;

import com.companio.dto.TrustedContactResponse;
import com.companio.model.TrustedContact;
import com.companio.service.TrustedContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contacts")
public class TrustedContactController {

    @Autowired
    private TrustedContactService trustedContactService;

    @PostMapping
    public ResponseEntity<TrustedContactResponse> addContact(
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        String userEmail = authentication.getName();
        TrustedContact contact = trustedContactService.addContact(
                userEmail,
                body.get("name"),
                body.get("phoneNumber"),
                body.get("relationship")
        );
        return ResponseEntity.ok(new TrustedContactResponse(contact));
    }

    @GetMapping
    public ResponseEntity<List<TrustedContactResponse>> getMyContacts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<TrustedContactResponse> responses = trustedContactService.getContactsForUser(userEmail)
                .stream()
                .map(TrustedContactResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}