package com.hospital_management.dtos;


import com.hospital_management.models.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long medicalRecordId;
    private Long appointmentId;
    private String prescriptionNumber;
    private LocalDateTime prescribedDate;
    private LocalDate validUntil;
    private String diagnosis;
    private String patientInstructions;
    private String pharmacyNotes;
    private String additionalNotes;
    private Prescription.PrescriptionStatus status;
    private Prescription.PrescriptionPriority priority;
    private Boolean isRepeatable;
    private Integer repeatCount;
    private Integer repeatsUsed;
    private LocalDateTime dispensedDate;
    private Long dispensedBy;
    private String pharmacyName;
    private String pharmacyContact;
    private Boolean isElectronic;
    private String qrCode;
    private String digitalSignature;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Related data
    private List<PrescriptionItemDto> prescriptionItems;

    // Computed fields
    private String statusDisplayName;
    private String priorityDisplayName;
    private Boolean isExpired;
    private Boolean canBeDispensed;
    private Boolean hasRepeatsAvailable;
    private Integer daysUntilExpiry;
    private String validityStatus;

    // Helper methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : null;
    }

    public Boolean getIsExpired() {
        return validUntil != null && validUntil.isBefore(LocalDate.now());
    }

    public Boolean getCanBeDispensed() {
        return status == Prescription.PrescriptionStatus.ACTIVE && !getIsExpired();
    }

    public Boolean getHasRepeatsAvailable() {
        return isRepeatable != null && isRepeatable &&
                repeatCount != null && repeatsUsed != null &&
                repeatsUsed < repeatCount;
    }

    public Integer getDaysUntilExpiry() {
        if (validUntil == null) return null;
        return (int) LocalDate.now().until(validUntil).getDays();
    }

    public String getValidityStatus() {
        if (getIsExpired()) return "Expired";
        Integer days = getDaysUntilExpiry();
        if (days == null) return "No expiry set";
        if (days <= 7) return "Expires soon";
        return "Valid";
    }
}
