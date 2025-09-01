package com.hospital_management.exception;

public class InsuranceNotFoundException extends RuntimeException {
    public InsuranceNotFoundException(Long insuranceId) {
        super("Insurance not found with id: " + insuranceId);
    }
}