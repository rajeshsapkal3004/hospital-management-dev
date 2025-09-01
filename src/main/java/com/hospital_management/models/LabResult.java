package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "lab_results")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "labTest"})
@ToString(exclude = {"patient", "labTest"})
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id")
    private LabTest labTest;

    @NotBlank(message = "Test type is required")
    @Size(max = 100, message = "Test type cannot exceed 100 characters")
    @Column(name = "test_type", nullable = false, length = 100)
    private String testType;

    @NotBlank(message = "Test name is required")
    @Size(max = 200, message = "Test name cannot exceed 200 characters")
    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @NotNull(message = "Test date is required")
    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;

    @Size(max = 50, message = "Sample type cannot exceed 50 characters")
    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(name = "sample_collected_date")
    private LocalDateTime sampleCollectedDate;

    @Size(max = 2000, message = "Results cannot exceed 2000 characters")
    @Column(name = "results", length = 2000)
    private String results;

    @Size(max = 500, message = "Normal range cannot exceed 500 characters")
    @Column(name = "normal_range", length = 500)
    private String normalRange;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    @Column(name = "unit", length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private ResultStatus status = ResultStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_flag", length = 20)
    private ResultFlag resultFlag;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @Size(max = 1000, message = "Interpretation cannot exceed 1000 characters")
    @Column(name = "interpretation", length = 1000)
    private String interpretation;

    @Column(name = "technician_id")
    private Long technicianId;

    @Size(max = 100, message = "Technician name cannot exceed 100 characters")
    @Column(name = "technician_name", length = 100)
    private String technicianName;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Size(max = 100, message = "Doctor name cannot exceed 100 characters")
    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Size(max = 100, message = "Verified by name cannot exceed 100 characters")
    @Column(name = "verified_by_name", length = 100)
    private String verifiedByName;

    @Builder.Default
    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;

    @Builder.Default
    @Column(name = "is_abnormal", nullable = false)
    private Boolean isAbnormal = false;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    @Column(name = "reference_number", unique = true, length = 100)
    private String referenceNumber;

    @Size(max = 200, message = "Report file path cannot exceed 200 characters")
    @Column(name = "report_file_path", length = 200)
    private String reportFilePath;

    // Store additional test values as JSON
    @ElementCollection
    @CollectionTable(name = "lab_result_values", joinColumns = @JoinColumn(name = "lab_result_id"))
    @MapKeyColumn(name = "parameter_name")
    @Column(name = "parameter_value")
    @Builder.Default
    private Map<String, String> testValues = new HashMap<>();

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
    public enum ResultStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        VERIFIED("Verified"),
        CANCELLED("Cancelled"),
        REJECTED("Rejected");

        private final String displayName;

        ResultStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ResultFlag {
        NORMAL("Normal"),
        HIGH("High"),
        LOW("Low"),
        CRITICAL_HIGH("Critical High"),
        CRITICAL_LOW("Critical Low"),
        ABNORMAL("Abnormal");

        private final String displayName;

        ResultFlag(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public boolean isCompleted() {
        return status == ResultStatus.COMPLETED || status == ResultStatus.VERIFIED;
    }

    public boolean requiresAttention() {
        return isCritical || isAbnormal || resultFlag == ResultFlag.CRITICAL_HIGH ||
                resultFlag == ResultFlag.CRITICAL_LOW;
    }

    public void markAsCompleted() {
        this.status = ResultStatus.COMPLETED;
    }

    public void markAsVerified(Long verifiedById, String verifierName) {
        this.status = ResultStatus.VERIFIED;
        this.verifiedDate = LocalDateTime.now();
        this.verifiedBy = verifiedById;
        this.verifiedByName = verifierName;
    }

    public void addTestValue(String parameter, String value) {
        this.testValues.put(parameter, value);
    }

    @PrePersist
    protected void onCreate() {
        if (referenceNumber == null || referenceNumber.isEmpty()) {
            referenceNumber = generateReferenceNumber();
        }
    }

    private String generateReferenceNumber() {
        return "LAB-" + System.currentTimeMillis();
    }
}
