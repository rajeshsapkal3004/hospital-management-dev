package com.hospital_management.exception;

public class MedicalRecordSigningException extends RuntimeException {
    public MedicalRecordSigningException(String message) {
        super("Medical record signing error: " + message);
    }

    public MedicalRecordSigningException(String message, Throwable cause) {
        super("Medical record signing error: " + message, cause);
    }
}