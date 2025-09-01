package com.hospital_management.dtos;



import com.hospital_management.models.MedicalRecord;
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
public class MedicalRecordDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long appointmentId;
    private LocalDate recordDate;
    private LocalDateTime visitDateTime;
    private String recordType;
    private String recordNumber;
    private String chiefComplaint;
    private String historyOfPresentIllness;
    private String pastMedicalHistory;
    private String familyHistory;
    private String socialHistory;
    private String allergies;
    private String currentMedications;
    private String physicalExamination;
    private String clinicalFindings;
    private String diagnosis;
    private String icdCodes;
    private String treatmentPlan;
    private String proceduresPerformed;
    private String labTestsOrdered;
    private String imagingOrdered;
    private String followUpInstructions;
    private LocalDate followUpDate;
    private String additionalNotes;
    private MedicalRecord.RecordStatus recordStatus;
    private MedicalRecord.ConfidentialityLevel confidentialityLevel;

    // Vital Signs
    private Double temperature;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Integer oxygenSaturation;
    private Double weight;
    private Double height;
    private Double bmi;

    private Boolean isEmergency;
    private Boolean isFollowUp;
    private Long previousRecordId;
    private LocalDateTime dischargeDate;
    private String dischargeSummary;
    private LocalDateTime signedDate;
    private Long signedBy;
    private String signedByName;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;

    // Related data
    private List<MedicalRecordAttachmentDto> attachments;
    private List<PrescriptionDto> prescriptions;

    // Computed fields
    private String recordStatusDisplayName;
    private String confidentialityDisplayName;
    private Boolean canBeModified;
    private Boolean isSigned;
    private String vitalSignsSummary;
    private String bloodPressure;

    // Helper methods
    public String getRecordStatusDisplayName() {
        return recordStatus != null ? recordStatus.getDisplayName() : null;
    }

    public String getConfidentialityDisplayName() {
        return confidentialityLevel != null ? confidentialityLevel.getDisplayName() : null;
    }

    public Boolean getCanBeModified() {
        return recordStatus == MedicalRecord.RecordStatus.DRAFT ||
                recordStatus == MedicalRecord.RecordStatus.ACTIVE;
    }

    public Boolean getIsSigned() {
        return recordStatus == MedicalRecord.RecordStatus.SIGNED && signedDate != null;
    }

    public String getBloodPressure() {
        if (bloodPressureSystolic != null && bloodPressureDiastolic != null) {
            return bloodPressureSystolic + "/" + bloodPressureDiastolic;
        }
        return null;
    }
}

