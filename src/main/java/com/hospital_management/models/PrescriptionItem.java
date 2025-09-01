package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_items")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"prescription"})
@ToString(exclude = {"prescription"})
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @NotBlank(message = "Medication name is required")
    @Size(max = 200, message = "Medication name cannot exceed 200 characters")
    @Column(name = "medication_name", nullable = false, length = 200)
    private String medicationName;

    @Size(max = 100, message = "Generic name cannot exceed 100 characters")
    @Column(name = "generic_name", length = 100)
    private String genericName;

    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    @Column(name = "brand_name", length = 100)
    private String brandName;

    @NotBlank(message = "Strength is required")
    @Size(max = 50, message = "Strength cannot exceed 50 characters")
    @Column(name = "strength", nullable = false, length = 50)
    private String strength;

    @NotBlank(message = "Dosage form is required")
    @Size(max = 50, message = "Dosage form cannot exceed 50 characters")
    @Column(name = "dosage_form", nullable = false, length = 50)
    private String dosageForm; // tablet, capsule, syrup, injection, etc.

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    @Column(name = "unit", length = 20)
    private String unit; // tablets, ml, bottles, etc.

    @NotBlank(message = "Dosage instructions are required")
    @Size(max = 500, message = "Dosage instructions cannot exceed 500 characters")
    @Column(name = "dosage_instructions", nullable = false, length = 500)
    private String dosageInstructions;

    @Size(max = 100, message = "Frequency cannot exceed 100 characters")
    @Column(name = "frequency", length = 100)
    private String frequency; // Once daily, Twice daily, etc.

    @Size(max = 100, message = "Duration cannot exceed 100 characters")
    @Column(name = "duration", length = 100)
    private String duration; // 7 days, 2 weeks, etc.

    @Size(max = 200, message = "Route of administration cannot exceed 200 characters")
    @Column(name = "route", length = 200)
    private String route; // Oral, IV, IM, etc.

    @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @Size(max = 200, message = "Warnings cannot exceed 200 characters")
    @Column(name = "warnings", length = 200)
    private String warnings;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "substitution_allowed", length = 20)
    private SubstitutionPolicy substitutionAllowed = SubstitutionPolicy.ALLOWED;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @Column(name = "is_dispensed", nullable = false)
    private Boolean isDispensed = false;

    @Column(name = "dispensed_quantity")
    private Integer dispensedQuantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    // Enums
    public enum SubstitutionPolicy {
        ALLOWED("Generic substitution allowed"),
        NOT_ALLOWED("No substitution"),
        BRAND_ONLY("Brand name only");

        private final String displayName;

        SubstitutionPolicy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public void dispense(Integer dispensedQty) {
        this.dispensedQuantity = dispensedQty;
        this.remainingQuantity = this.quantity - dispensedQty;
        this.isDispensed = dispensedQty >= this.quantity;
    }

    public boolean isFullyDispensed() {
        return isDispensed && dispensedQuantity != null && dispensedQuantity >= quantity;
    }

    public boolean isPartiallyDispensed() {
        return dispensedQuantity != null && dispensedQuantity > 0 && dispensedQuantity < quantity;
    }

    @PrePersist
    @PreUpdate
    protected void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
        if (remainingQuantity == null) {
            remainingQuantity = quantity;
        }
    }
}
