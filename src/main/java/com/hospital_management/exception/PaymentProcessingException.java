package com.hospital_management.exception;



public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super("Payment processing error: " + message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super("Payment processing error: " + message, cause);
    }
}
