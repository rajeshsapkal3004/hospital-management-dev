package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"bill"})
@ToString(exclude = {"bill"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Payment amount must be a valid monetary value")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @NotNull(message = "Payment date is required")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refunded_date")
    private LocalDateTime refundedDate;

    @Digits(integer = 10, fraction = 2, message = "Refunded amount must be a valid monetary value")
    @Column(name = "refunded_amount", precision = 12, scale = 2)
    private BigDecimal refundedAmount;

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    // Payment Status Enum
    public enum PaymentStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED && refundedAmount == null;
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedDate = LocalDateTime.now();
    }

    public void refund(BigDecimal refundAmount) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAmount = refundAmount;
        this.refundedDate = LocalDateTime.now();
    }
}

