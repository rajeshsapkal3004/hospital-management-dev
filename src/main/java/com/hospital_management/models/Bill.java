package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "payments"})
@ToString(exclude = {"patient", "payments"})
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Bill number is required")
    @Size(max = 50, message = "Bill number cannot exceed 50 characters")
    @Column(name = "bill_number", unique = true, nullable = false, length = 50)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @NotNull(message = "Bill date is required")
    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must be a valid monetary value")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Paid amount must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Paid amount must be a valid monetary value")
    @Builder.Default
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tax amount must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must be a valid monetary value")
    @Builder.Default
    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must be a valid monetary value")
    @Builder.Default
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private BillStatus status = BillStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "bill_type", nullable = false, length = 30)
    private BillType billType = BillType.CONSULTATION;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Size(max = 100, message = "Insurance claim number cannot exceed 100 characters")
    @Column(name = "insurance_claim_number", length = 100)
    private String insuranceClaimNumber;

    @Builder.Default
    @Column(name = "is_insurance_covered", nullable = false)
    private Boolean isInsuranceCovered = false;

    @Digits(integer = 10, fraction = 2, message = "Insurance amount must be a valid monetary value")
    @Column(name = "insurance_amount", precision = 12, scale = 2)
    private BigDecimal insuranceAmount;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    // One-to-Many relationship with payments
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    // Enums
    public enum BillStatus {
        PENDING("Pending"),
        PARTIAL_PAID("Partially Paid"),
        PAID("Paid"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        BillStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BillType {
        CONSULTATION("Consultation"),
        PROCEDURE("Procedure"),
        LABORATORY("Laboratory"),
        PHARMACY("Pharmacy"),
        ROOM_CHARGES("Room Charges"),
        SURGERY("Surgery"),
        EMERGENCY("Emergency"),
        ADMISSION("Admission"),
        OTHER("Other");

        private final String displayName;

        BillType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    public boolean isFullyPaid() {
        return paidAmount.compareTo(totalAmount) >= 0;
    }

    public boolean isOverdue() {
        return dueDate.isBefore(LocalDate.now()) && status != BillStatus.PAID;
    }

    public BigDecimal getPaymentPercentage() {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return paidAmount.multiply(new BigDecimal("100")).divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP);
    }

    public void addPayment(Payment payment) {
        this.payments.add(payment);
        payment.setBill(this);
        updatePaidAmount();
    }

    private void updatePaidAmount() {
        this.paidAmount = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        updateStatus();
    }

    private void updateStatus() {
        if (isFullyPaid()) {
            this.status = BillStatus.PAID;
            this.paidDate = LocalDateTime.now();
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = BillStatus.PARTIAL_PAID;
        } else if (isOverdue()) {
            this.status = BillStatus.OVERDUE;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (billNumber == null || billNumber.isEmpty()) {
            billNumber = generateBillNumber();
        }
    }

    private String generateBillNumber() {
        return "BILL-" + System.currentTimeMillis();
    }
}

