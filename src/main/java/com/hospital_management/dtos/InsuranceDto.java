package com.hospital_management.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDto {

    private Long id;
    private Long patientId;

    @NotBlank(message = "Insurance provider is required")
    @Size(max = 100, message = "Insurance provider cannot exceed 100 characters")
    private String insuranceProvider;

    @NotBlank(message = "Policy number is required")
    @Size(max = 50, message = "Policy number cannot exceed 50 characters")
    private String policyNumber;

    @Size(max = 50, message = "Group number cannot exceed 50 characters")
    private String groupNumber;

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    private String planName;

    @Size(max = 100, message = "Subscriber name cannot exceed 100 characters")
    private String subscriberName;

    @Size(max = 100, message = "Subscriber ID cannot exceed 100 characters")
    private String subscriberId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate effectiveDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expirationDate;

    @Builder.Default
    private Boolean isPrimary = true;

    @Builder.Default
    private Boolean isActive = true;

    private String copayAmount;
    private String deductibleAmount;
    private String coverageDetails;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Computed fields
    private Boolean isExpired;
    private Integer daysUntilExpiry;

    public Boolean getIsExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    public Integer getDaysUntilExpiry() {
        if (expirationDate == null) return null;
        return (int) LocalDate.now().until(expirationDate).getDays();
    }
}