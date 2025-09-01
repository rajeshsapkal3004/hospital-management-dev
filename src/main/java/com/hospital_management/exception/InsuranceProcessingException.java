package com.hospital_management.exception;



public class InsuranceProcessingException extends RuntimeException {
    public InsuranceProcessingException(String claimNumber) {
        super("Insurance processing failed for claim: " + claimNumber);
    }

    public InsuranceProcessingException(String message, Throwable cause) {
        super("Insurance processing error: " + message, cause);
    }
}

