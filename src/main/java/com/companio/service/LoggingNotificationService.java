package com.companio.service;

import com.companio.model.SosEvent;
import com.companio.model.TrustedContact;
import com.companio.repository.TrustedContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logs SOS alerts and sends each one to the contact's own Telegram chat when connected.
 */
@Service
public class LoggingNotificationService implements NotificationService {

    @Autowired
    private TrustedContactRepository trustedContactRepository;

    @Autowired
    private TelegramClient telegram;

    @Value("${companio.public-base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void notifyContacts(SosEvent event) {

        List<TrustedContact> contacts =
                trustedContactRepository.findByUser_Id(event.getUser().getId());

        if (contacts.isEmpty()) {
            System.out.println("SOS " + event.getId()
                    + ": user has no trusted contacts to alert.");
            return;
        }

        String location =
                (event.getLatitude() != null && event.getLongitude() != null)
                        ? "https://maps.google.com/?q="
                        + event.getLatitude() + "," + event.getLongitude()
                        : "location unavailable";

        String tracking = event.getTrackingToken() != null
                ? baseUrl + "/track.html?t=" + event.getTrackingToken()
                : "no tracking link";

        String message = event.getUser().getName() + " needs help (SOS)."
                + "\nLive tracking: " + tracking
                + "\nStarting position: " + location;

        for (TrustedContact contact : contacts) {
            boolean viaTelegram = contact.getTelegramChatId() != null && telegram.enabled();
            System.out.println("[ALERT] SOS " + event.getId()
                    + ": " + contact.getName()
                    + " (" + contact.getPhoneNumber() + ") via "
                    + (viaTelegram ? "Telegram" : "log only (Telegram not connected)")
                    + " | live tracking: " + tracking);
            if (viaTelegram) {
                telegram.send(contact.getTelegramChatId(), message);
            }
        }
    }
}