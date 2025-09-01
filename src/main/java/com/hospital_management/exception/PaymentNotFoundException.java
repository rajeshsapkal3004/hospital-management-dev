package com.hospital_management.exception;



public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found with id: " + paymentId);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
