package com.companio.dto;

import com.companio.model.TrustedContact;
/**
 * Response payload for a trusted-contact record.
 */


public class TrustedContactResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String relationship;

    public TrustedContactResponse(TrustedContact contact) {
        this.id = contact.getId();
        this.name = contact.getName();
        this.phoneNumber = contact.getPhoneNumber();
        this.relationship = contact.getRelationship();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRelationship() { return relationship; }
}