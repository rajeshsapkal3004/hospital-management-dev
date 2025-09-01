package com.hospital_management.exception;

public class LabResultProcessingException extends RuntimeException {
    public LabResultProcessingException(String message) {
        super("Lab result processing error: " + message);
    }

    public LabResultProcessingException(String message, Throwable cause) {
        super("Lab result processing error: " + message, cause);
    }
}