package com.companio.model;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Represents a trusted contact linked to a user account.
 */
@Entity
@Table(name = "trusted_contacts")
public class TrustedContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    private String relationship; // e.g. "Mother", "Friend", "Roommate"

    // Secret code used in the personal Telegram connect link.
    @Column(unique = true)
    private String connectCode;

    // Set when the contact presses Start on the bot. Null means not connected.
    private String telegramChatId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public TrustedContact() {
    }

    public TrustedContact(String name, String phoneNumber, String relationship, User user) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.user = user;
        this.connectCode = newCode();
    }

    public static String newCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getConnectCode() { return connectCode; }
    public void setConnectCode(String connectCode) { this.connectCode = connectCode; }

    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}