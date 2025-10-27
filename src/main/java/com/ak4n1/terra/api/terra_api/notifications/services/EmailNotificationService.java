package com.ak4n1.terra.api.terra_api.notifications.services;

public interface EmailNotificationService {
    void sendEmail(String to, String subject, String body);
} 