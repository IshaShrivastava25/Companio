package com.companio.service;

import com.companio.model.SosEvent;

public interface NotificationService {
    void notifyContacts(SosEvent event);
}