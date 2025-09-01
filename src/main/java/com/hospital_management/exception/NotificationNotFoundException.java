package com.hospital_management.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long notificationId) {
        super("Notification not found with id: " + notificationId);
    }
}