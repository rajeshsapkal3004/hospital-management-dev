package com.hospital_management.exception;

public class MedicalRecordModificationException extends RuntimeException {
    public MedicalRecordModificationException(String message) {
        super("Medical record modification error: " + message);
    }

    public MedicalRecordModificationException(String message, Throwable cause) {
        super("Medical record modification error: " + message, cause);
    }
}