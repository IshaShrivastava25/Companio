package com.companio.service;

import com.companio.model.SosEvent;
import com.companio.model.TrustedContact;
import com.companio.repository.TrustedContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoggingNotificationService implements NotificationService {

    @Autowired
    private TrustedContactRepository trustedContactRepository;

    @Override
    public void notifyContacts(SosEvent event) {
        List<TrustedContact> contacts =
                trustedContactRepository.findByUser_Id(event.getUser().getId());

        if (contacts.isEmpty()) {
            System.out.println("SOS " + event.getId() + ": user has no trusted contacts to alert.");
            return;
        }

        String location = (event.getLatitude() != null && event.getLongitude() != null)
                ? "https://maps.google.com/?q=" + event.getLatitude() + "," + event.getLongitude()
                : "location unavailable";

        for (TrustedContact contact : contacts) {
            System.out.println("[ALERT] SOS " + event.getId() + ": would notify "
                    + contact.getName() + " (" + contact.getPhoneNumber() + ") - "
                    + event.getUser().getName() + " needs help. " + location);
        }
    }
}