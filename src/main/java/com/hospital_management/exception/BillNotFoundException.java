package com.hospital_management.exception;


public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(Long billId) {
        super("Bill not found with id: " + billId);
    }

    public BillNotFoundException(String message) {
        super(message);
    }
}

