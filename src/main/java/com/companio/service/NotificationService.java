package com.companio.service;

import com.companio.model.SosEvent;
/**
 * Defines the notification interface used by safety alerts.
 */


public interface NotificationService {
    void notifyContacts(SosEvent event);
}