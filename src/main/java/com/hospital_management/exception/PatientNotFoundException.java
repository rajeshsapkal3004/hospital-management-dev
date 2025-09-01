package com.hospital_management.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long patientId) {
        super("Patient not found with id: " + patientId);
    }

    public PatientNotFoundException(String message) {
        super(message);
    }
}
