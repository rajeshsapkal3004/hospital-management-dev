package com.hospital_management.exception;

public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(Long prescriptionId) {
        super("Prescription not found with id: " + prescriptionId);
    }

    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}