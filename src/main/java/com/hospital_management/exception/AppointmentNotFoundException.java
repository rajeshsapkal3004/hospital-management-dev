package com.hospital_management.exception;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(Long id) {
        super("Appointment not found with id: " + id);
    }

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}