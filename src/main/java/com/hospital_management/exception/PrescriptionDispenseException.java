package com.hospital_management.exception;

public class PrescriptionDispenseException extends RuntimeException {
    public PrescriptionDispenseException(String message) {
        super("Prescription dispensing error: " + message);
    }

    public PrescriptionDispenseException(String message, Throwable cause) {
        super("Prescription dispensing error: " + message, cause);
    }
}