package com.hospital_management.exception;

public class MedicalRecordAccessDeniedException extends RuntimeException {
    public MedicalRecordAccessDeniedException(Long recordId, Long userId) {
        super("Access denied to medical record " + recordId + " for user " + userId);
    }

    public MedicalRecordAccessDeniedException(String message) {
        super(message);
    }
}