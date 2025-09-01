package com.hospital_management.exception;

public class LabTestNotFoundException extends RuntimeException {
    public LabTestNotFoundException(Long testId) {
        super("Lab test not found with id: " + testId);
    }

    public LabTestNotFoundException(String message) {
        super(message);
    }
}