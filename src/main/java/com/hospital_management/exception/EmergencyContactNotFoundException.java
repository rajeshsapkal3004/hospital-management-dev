package com.hospital_management.exception;

public class EmergencyContactNotFoundException extends RuntimeException {
    public EmergencyContactNotFoundException(Long contactId) {
        super("Emergency contact not found with id: " + contactId);
    }
}