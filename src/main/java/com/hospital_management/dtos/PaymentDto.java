package com.hospital_management.dtos;


import com.hospital_management.models.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long patientId; // Set from authentication

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Payment amount must be a valid monetary value")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String paymentMethod;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String referenceNumber;

    // For credit card payments
    @Size(max = 20, message = "Card number cannot exceed 20 characters")
    private String cardNumber;

    @Size(max = 100, message = "Card holder name cannot exceed 100 characters")
    private String cardHolderName;

    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;

    @Min(value = 2024, message = "Expiry year must be current year or later")
    private Integer expiryYear;

    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    private String cvv;

    // For bank transfer
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;

    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    private String ifscCode;

    // Payment method constants
    public static final String CASH = "CASH";
    public static final String CARD = "CARD";
    public static final String BANK_TRANSFER = "BANK_TRANSFER";
    public static final String UPI = "UPI";
    public static final String WALLET = "WALLET";
    public static final String INSURANCE = "INSURANCE";
}
