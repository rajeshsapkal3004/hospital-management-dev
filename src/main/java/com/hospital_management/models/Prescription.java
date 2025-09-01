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
import java.util.List;

@Entity
@Table(name = "prescriptions")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "doctor", "medicalRecord", "prescriptionItems"})
@ToString(exclude = {"patient", "doctor", "medicalRecord", "prescriptionItems"})
public class Prescription {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @NotBlank(message = "Prescription number is required")
    @Size(max = 100, message = "Prescription number cannot exceed 100 characters")
    @Column(name = "prescription_number", unique = true, nullable = false, length = 100)
    private String prescriptionNumber;

    @NotNull(message = "Prescribed date is required")
    @Column(name = "prescribed_date", nullable = false)
    private LocalDateTime prescribedDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Size(max = 2000, message = "Diagnosis cannot exceed 2000 characters")
    @Column(name = "diagnosis", length = 2000)
    private String diagnosis;

    @Size(max = 1000, message = "Patient instructions cannot exceed 1000 characters")
    @Column(name = "patient_instructions", length = 1000)
    private String patientInstructions;

    @Size(max = 1000, message = "Pharmacy notes cannot exceed 1000 characters")
    @Column(name = "pharmacy_notes", length = 1000)
    private String pharmacyNotes;

    @Size(max = 500, message = "Additional notes cannot exceed 500 characters")
    @Column(name = "additional_notes", length = 500)
    private String additionalNotes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", nullable = false, length = 20)
    private PrescriptionPriority priority = PrescriptionPriority.NORMAL;

    @Builder.Default
    @Column(name = "is_repeatable", nullable = false)
    private Boolean isRepeatable = false;

    @Column(name = "repeat_count")
    private Integer repeatCount;

    @Column(name = "repeats_used")
    @Builder.Default
    private Integer repeatsUsed = 0;

    @Column(name = "dispensed_date")
    private LocalDateTime dispensedDate;

    @Column(name = "dispensed_by")
    private Long dispensedBy;

    @Size(max = 100, message = "Pharmacy name cannot exceed 100 characters")
    @Column(name = "pharmacy_name", length = 100)
    private String pharmacyName;

    @Size(max = 100, message = "Pharmacy contact cannot exceed 100 characters")
    @Column(name = "pharmacy_contact", length = 100)
    private String pharmacyContact;

    @Builder.Default
    @Column(name = "is_electronic", nullable = false)
    private Boolean isElectronic = true;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "digital_signature")
    private String digitalSignature;

    // One-to-Many relationship with prescription items
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();

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
    public enum PrescriptionStatus {
        ACTIVE("Active"),
        DISPENSED("Dispensed"),
        PARTIALLY_DISPENSED("Partially Dispensed"),
        EXPIRED("Expired"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed");

        private final String displayName;

        PrescriptionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PrescriptionPriority {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        PrescriptionPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business methods
    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDate.now());
    }

    public boolean canBeDispensed() {
        return status == PrescriptionStatus.ACTIVE && !isExpired();
    }

    public boolean hasRepeatsAvailable() {
        return isRepeatable && repeatCount != null && repeatsUsed < repeatCount;
    }

    public void markAsDispensed(Long pharmacistId, String pharmacy) {
        this.status = PrescriptionStatus.DISPENSED;
        this.dispensedDate = LocalDateTime.now();
        this.dispensedBy = pharmacistId;
        this.pharmacyName = pharmacy;
    }

    public void useRepeat() {
        if (hasRepeatsAvailable()) {
            this.repeatsUsed++;
            if (this.repeatsUsed >= this.repeatCount) {
                this.status = PrescriptionStatus.COMPLETED;
            }
        }
    }

    public void addPrescriptionItem(PrescriptionItem item) {
        this.prescriptionItems.add(item);
        item.setPrescription(this);
    }

    public void cancel(String reason) {
        this.status = PrescriptionStatus.CANCELLED;
        this.additionalNotes = (this.additionalNotes != null ? this.additionalNotes + ". " : "") +
                "Cancelled: " + reason;
    }

    @PrePersist
    protected void onCreate() {
        if (prescriptionNumber == null || prescriptionNumber.isEmpty()) {
            prescriptionNumber = generatePrescriptionNumber();
        }
        if (validUntil == null) {
            validUntil = LocalDate.now().plusMonths(6); // Default 6 months validity
        }
    }

    private String generatePrescriptionNumber() {
        return "RX-" + System.currentTimeMillis();
    }
}
