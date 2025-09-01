package com.hospital_management.models;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "medical_records")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "doctor", "attachments", "prescriptions"})
@ToString(exclude = {"patient", "doctor", "attachments", "prescriptions"})
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "doctor_id", insertable = false, updatable = false)
    private Long doctorId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @NotNull(message = "Record date is required")
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @NotNull(message = "Visit date and time is required")
    @Column(name = "visit_date_time", nullable = false)
    private LocalDateTime visitDateTime;

    @NotBlank(message = "Record type is required")
    @Size(max = 50, message = "Record type cannot exceed 50 characters")
    @Column(name = "record_type", nullable = false, length = 50)
    private String recordType;

    @Size(max = 100, message = "Record number cannot exceed 100 characters")
    @Column(name = "record_number", unique = true, length = 100)
    private String recordNumber;

    // Convert large text fields to TEXT type using @Lob
    @Lob
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Lob
    @Column(name = "history_of_present_illness", columnDefinition = "TEXT")
    private String historyOfPresentIllness;

    @Lob
    @Column(name = "past_medical_history", columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Size(max = 1000, message = "Family history cannot exceed 1000 characters")
    @Column(name = "family_history", length = 1000)
    private String familyHistory;

    @Size(max = 1000, message = "Social history cannot exceed 1000 characters")
    @Column(name = "social_history", length = 1000)
    private String socialHistory;

    @Size(max = 1000, message = "Allergies cannot exceed 1000 characters")
    @Column(name = "allergies", length = 1000)
    private String allergies;

    @Size(max = 1000, message = "Current medications cannot exceed 1000 characters")
    @Column(name = "current_medications", length = 1000)
    private String currentMedications;

    @Lob
    @Column(name = "physical_examination", columnDefinition = "TEXT")
    private String physicalExamination;

    @Lob
    @Column(name = "clinical_findings", columnDefinition = "TEXT")
    private String clinicalFindings;

    @Lob
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Size(max = 1000, message = "ICD codes cannot exceed 1000 characters")
    @Column(name = "icd_codes", length = 1000)
    private String icdCodes;

    @Lob
    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Lob
    @Column(name = "procedures_performed", columnDefinition = "TEXT")
    private String proceduresPerformed;

    @Size(max = 1000, message = "Lab tests ordered cannot exceed 1000 characters")
    @Column(name = "lab_tests_ordered", length = 1000)
    private String labTestsOrdered;

    @Size(max = 1000, message = "Imaging ordered cannot exceed 1000 characters")
    @Column(name = "imaging_ordered", length = 1000)
    private String imagingOrdered;

    @Lob
    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Lob
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "record_status", nullable = false, length = 20)
    private RecordStatus recordStatus = RecordStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "confidentiality_level", nullable = false, length = 20)
    private ConfidentialityLevel confidentialityLevel = ConfidentialityLevel.NORMAL;

    // Vital Signs - keep as regular columns since they're small
    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "bmi")
    private Double bmi;

    @Builder.Default
    @Column(name = "is_emergency", nullable = false)
    private Boolean isEmergency = false;

    @Builder.Default
    @Column(name = "is_follow_up", nullable = false)
    private Boolean isFollowUp = false;

    @Column(name = "previous_record_id")
    private Long previousRecordId;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Lob
    @Column(name = "discharge_summary", columnDefinition = "TEXT")
    private String dischargeSummary;

    @Column(name = "signed_date")
    private LocalDateTime signedDate;

    @Column(name = "signed_by")
    private Long signedBy;

    // Relationships
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicalRecordAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

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
    public enum RecordStatus {
        DRAFT("Draft"),
        ACTIVE("Active"),
        SIGNED("Signed"),
        AMENDED("Amended"),
        CORRECTED("Corrected"),
        CANCELLED("Cancelled"),
        ARCHIVED("Archived");

        private final String displayName;

        RecordStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ConfidentialityLevel {
        NORMAL("Normal"),
        RESTRICTED("Restricted"),
        CONFIDENTIAL("Confidential"),
        VERY_RESTRICTED("Very Restricted");

        private final String displayName;

        ConfidentialityLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public boolean canBeModified() {
        return recordStatus == RecordStatus.DRAFT || recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isSigned() {
        return recordStatus == RecordStatus.SIGNED && signedDate != null;
    }

    public void sign(Long doctorId) {
        this.recordStatus = RecordStatus.SIGNED;
        this.signedDate = LocalDateTime.now();
        this.signedBy = doctorId;
    }

    public void calculateBMI() {
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = weight / (heightInMeters * heightInMeters);
        }
    }

    public void addAttachment(MedicalRecordAttachment attachment) {
        this.attachments.add(attachment);
        attachment.setMedicalRecord(this);
    }

    public void addPrescription(Prescription prescription) {
        this.prescriptions.add(prescription);
        prescription.setMedicalRecord(this);
    }

    @PrePersist
    protected void onCreate() {
        if (recordNumber == null || recordNumber.isEmpty()) {
            recordNumber = generateRecordNumber();
        }
        calculateBMI();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateBMI();
    }

    private String generateRecordNumber() {
        return "MR-" + System.currentTimeMillis();
    }
}
