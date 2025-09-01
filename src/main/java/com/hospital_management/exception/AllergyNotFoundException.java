package com.hospital_management.exception;

public class AllergyNotFoundException extends RuntimeException {
    public AllergyNotFoundException(Long allergyId) {
        super("Allergy not found with id: " + allergyId);
    }
}
