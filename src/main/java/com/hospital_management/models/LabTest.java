package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_tests")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "doctor"})
@ToString(exclude = {"patient", "doctor"})
public class LabTest {

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

    @NotBlank(message = "Test type is required")
    @Size(max = 100, message = "Test type cannot exceed 100 characters")
    @Column(name = "test_type", nullable = false, length = 100)
    private String testType;

    @NotBlank(message = "Test name is required")
    @Size(max = 200, message = "Test name cannot exceed 200 characters")
    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @NotNull(message = "Order date is required")
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Size(max = 50, message = "Sample type cannot exceed 50 characters")
    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Size(max = 1000, message = "Instructions cannot exceed 1000 characters")
    @Column(name = "instructions", length = 1000)
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private TestStatus status = TestStatus.ORDERED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "urgency", nullable = false, length = 20)
    private TestUrgency urgency = TestUrgency.ROUTINE;

    @Size(max = 100, message = "Order number cannot exceed 100 characters")
    @Column(name = "order_number", unique = true, length = 100)
    private String orderNumber;

    @Size(max = 500, message = "Clinical info cannot exceed 500 characters")
    @Column(name = "clinical_info", length = 500)
    private String clinicalInfo;

    @Column(name = "sample_collected_date")
    private LocalDateTime sampleCollectedDate;

    @Column(name = "sample_collected_by")
    private Long sampleCollectedBy;

    @Column(name = "estimated_completion_date")
    private LocalDateTime estimatedCompletionDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Builder.Default
    @Column(name = "is_fasting_required", nullable = false)
    private Boolean isFastingRequired = false;

    @Builder.Default
    @Column(name = "is_home_collection", nullable = false)
    private Boolean isHomeCollection = false;

    @Size(max = 200, message = "Collection address cannot exceed 200 characters")
    @Column(name = "collection_address", length = 200)
    private String collectionAddress;

    // One-to-Many relationship with lab results
    @OneToMany(mappedBy = "labTest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LabResult> labResults = new ArrayList<>();

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
    public enum TestStatus {
        ORDERED("Ordered"),
        SCHEDULED("Scheduled"),
        SAMPLE_COLLECTED("Sample Collected"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        REJECTED("Rejected");

        private final String displayName;

        TestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TestUrgency {
        ROUTINE("Routine"),
        URGENT("Urgent"),
        STAT("STAT"),
        EMERGENCY("Emergency");

        private final String displayName;

        TestUrgency(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public boolean canCollectSample() {
        return status == TestStatus.ORDERED || status == TestStatus.SCHEDULED;
    }

    public boolean isCompleted() {
        return status == TestStatus.COMPLETED;
    }

    public void markSampleCollected(Long collectedById) {
        this.status = TestStatus.SAMPLE_COLLECTED;
        this.sampleCollectedDate = LocalDateTime.now();
        this.sampleCollectedBy = collectedById;
    }

    public void markCompleted() {
        this.status = TestStatus.COMPLETED;
    }

    public void cancel() {
        this.status = TestStatus.CANCELLED;
    }

    @PrePersist
    protected void onCreate() {
        if (orderNumber == null || orderNumber.isEmpty()) {
            orderNumber = generateOrderNumber();
        }
    }

    private String generateOrderNumber() {
        return "TEST-" + System.currentTimeMillis();
    }
}
