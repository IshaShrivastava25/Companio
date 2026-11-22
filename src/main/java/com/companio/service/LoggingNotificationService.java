package com.companio.service;

import com.companio.model.SosEvent;
import com.companio.model.TrustedContact;
import com.companio.repository.TrustedContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logs SOS and timer alerts for the prototype notification system.
 */
@Service
public class LoggingNotificationService implements NotificationService {

    @Autowired
    private TrustedContactRepository trustedContactRepository;

    @Value("${companio.public-base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void notifyContacts(SosEvent event) {

        List<TrustedContact> contacts =
                trustedContactRepository.findByUser_Id(
                        event.getUser().getId()
                );

        if (contacts.isEmpty()) {
            System.out.println(
                    "SOS " + event.getId()
                            + ": user has no trusted contacts to alert."
            );
            return;
        }

        String location =
                (event.getLatitude() != null && event.getLongitude() != null)
                        ? "https://maps.google.com/?q="
                        + event.getLatitude() + "," + event.getLongitude()
                        : "location unavailable";

        // Generate the live tracking link if a tracking token exists.
        String tracking = event.getTrackingToken() != null
                ? baseUrl + "/track.html?t=" + event.getTrackingToken()
                : "no tracking link";

        for (TrustedContact contact : contacts) {

            System.out.println(
                    "[ALERT] SOS " + event.getId()
                            + ": would notify "
                            + contact.getName()
                            + " (" + contact.getPhoneNumber() + ") - "
                            + event.getUser().getName()
                            + " needs help. "
                            + location
                            + " | live tracking: "
                            + tracking
            );
        }
    }
}
