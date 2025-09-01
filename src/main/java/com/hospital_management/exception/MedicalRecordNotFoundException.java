package com.hospital_management.exception;


public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(Long recordId) {
        super("Medical record not found with id: " + recordId);
    }

    public MedicalRecordNotFoundException(String message) {
        super(message);
    }
}