package com.hospital_management.exception;

public class InvalidPrescriptionException extends RuntimeException {
    public InvalidPrescriptionException(String message) {
        super("Invalid prescription: " + message);
    }

    public InvalidPrescriptionException(String message, Throwable cause) {
        super("Invalid prescription: " + message, cause);
    }
}