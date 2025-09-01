package com.hospital_management.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyDto {

    private Long id;
    private Long patientId;
    private String allergen;
    private String allergyType; // DRUG, FOOD, ENVIRONMENTAL, etc.
    private String severity; // MILD, MODERATE, SEVERE
    private String reaction;
    private String notes;
    private LocalDateTime onsetDate;
    private Boolean isActive;
    private String reportedBy;
    private LocalDateTime reportedDate;
    private String verifiedBy;
    private LocalDateTime verifiedDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Computed fields
    private String severityDisplayName;
    private String allergyTypeDisplayName;
    private Boolean requiresAttention;

    public String getSeverityDisplayName() {
        if (severity == null) return null;
        switch (severity.toUpperCase()) {
            case "MILD": return "Mild";
            case "MODERATE": return "Moderate";
            case "SEVERE": return "Severe";
            default: return severity;
        }
    }

    public String getAllergyTypeDisplayName() {
        if (allergyType == null) return null;
        switch (allergyType.toUpperCase()) {
            case "DRUG": return "Drug Allergy";
            case "FOOD": return "Food Allergy";
            case "ENVIRONMENTAL": return "Environmental";
            case "CONTACT": return "Contact Allergy";
            default: return allergyType;
        }
    }

    public Boolean getRequiresAttention() {
        return "SEVERE".equalsIgnoreCase(severity);
    }
}
