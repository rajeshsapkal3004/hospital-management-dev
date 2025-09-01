package com.hospital_management.dtos;

import com.hospital_management.models.PrescriptionItem;
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
public class CreatePrescriptionItemDto {

    @NotBlank(message = "Medication name is required")
    @Size(max = 200, message = "Medication name cannot exceed 200 characters")
    private String medicationName;

    @Size(max = 100, message = "Generic name cannot exceed 100 characters")
    private String genericName;

    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    private String brandName;

    @NotBlank(message = "Strength is required")
    @Size(max = 50, message = "Strength cannot exceed 50 characters")
    private String strength;

    @NotBlank(message = "Dosage form is required")
    @Size(max = 50, message = "Dosage form cannot exceed 50 characters")
    private String dosageForm;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    @NotBlank(message = "Dosage instructions are required")
    @Size(max = 500, message = "Dosage instructions cannot exceed 500 characters")
    private String dosageInstructions;

    @Size(max = 100, message = "Frequency cannot exceed 100 characters")
    private String frequency;

    @Size(max = 100, message = "Duration cannot exceed 100 characters")
    private String duration;

    @Size(max = 200, message = "Route cannot exceed 200 characters")
    private String route;

    @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
    private String specialInstructions;

    @Size(max = 200, message = "Warnings cannot exceed 200 characters")
    private String warnings;

    @Builder.Default
    private PrescriptionItem.SubstitutionPolicy substitutionAllowed = PrescriptionItem.SubstitutionPolicy.ALLOWED;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Unit price must be a valid monetary value")
    private BigDecimal unitPrice;
}