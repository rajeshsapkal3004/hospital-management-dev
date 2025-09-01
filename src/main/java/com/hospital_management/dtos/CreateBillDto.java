package com.hospital_management.dtos;


import com.hospital_management.models.Bill;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long appointmentId;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must be a valid monetary value")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tax amount must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must be a valid monetary value")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must be a valid monetary value")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Builder.Default
    private Bill.BillType billType = Bill.BillType.CONSULTATION;

    @Builder.Default
    private Boolean isInsuranceCovered = false;

    @Size(max = 100, message = "Insurance claim number cannot exceed 100 characters")
    private String insuranceClaimNumber;

    @Digits(integer = 10, fraction = 2, message = "Insurance amount must be a valid monetary value")
    private BigDecimal insuranceAmount;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
