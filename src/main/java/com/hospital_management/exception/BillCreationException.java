package com.hospital_management.exception;



public class BillCreationException extends RuntimeException {
    public BillCreationException(String details) {
        super("Failed to create bill: " + details);
    }

    public BillCreationException(String details, Throwable cause) {
        super("Failed to create bill: " + details, cause);
    }
}

