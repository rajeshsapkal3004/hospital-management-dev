package com.hospital_management.exception;

public class PrescriptionExpiredException extends RuntimeException {
    public PrescriptionExpiredException(String prescriptionNumber) {
        super("Prescription has expired: " + prescriptionNumber);
    }

    public PrescriptionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}