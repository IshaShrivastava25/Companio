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
    private boolean telegramConnected;
    private String connectLink;

    public TrustedContactResponse(TrustedContact contact) {
        this(contact, "");
    }

    public TrustedContactResponse(TrustedContact contact, String botUsername) {
        this.id = contact.getId();
        this.name = contact.getName();
        this.phoneNumber = contact.getPhoneNumber();
        this.relationship = contact.getRelationship();
        this.telegramConnected = contact.getTelegramChatId() != null;
        this.connectLink = (!telegramConnected
                && botUsername != null && !botUsername.isBlank()
                && contact.getConnectCode() != null)
                ? "https://t.me/" + botUsername + "?start=" + contact.getConnectCode()
                : null;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRelationship() { return relationship; }
    public boolean isTelegramConnected() { return telegramConnected; }
    public String getConnectLink() { return connectLink; }
}
