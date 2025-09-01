package com.hospital_management.exception;

public class LabResultNotFoundException extends RuntimeException {
    public LabResultNotFoundException(Long resultId) {
        super("Lab result not found with id: " + resultId);
    }

    public LabResultNotFoundException(String message) {
        super(message);
    }
}
