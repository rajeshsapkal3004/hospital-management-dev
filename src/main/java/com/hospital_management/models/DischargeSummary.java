package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "discharge_summaries")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"patient", "medicalRecord"})
@ToString(exclude = {"patient", "medicalRecord"})
public class DischargeSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @NotNull(message = "Admission date is required")
    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @NotNull(message = "Discharge date is required")
    @Column(name = "discharge_date", nullable = false)
    private LocalDate dischargeDate;

    @Size(max = 1000, message = "Admission diagnosis cannot exceed 1000 characters")
    @Column(name = "admission_diagnosis", length = 1000)
    private String admissionDiagnosis;

    @Size(max = 1000, message = "Discharge diagnosis cannot exceed 1000 characters")
    @Column(name = "discharge_diagnosis", length = 1000)
    private String dischargeDiagnosis;

    @Size(max = 2000, message = "Treatment provided cannot exceed 2000 characters")
    @Column(name = "treatment_provided", length = 2000)
    private String treatmentProvided;

    @Size(max = 1000, message = "Discharge medications cannot exceed 1000 characters")
    @Column(name = "discharge_medications", length = 1000)
    private String dischargeMedications;

    @Size(max = 2000, message = "Follow up instructions cannot exceed 2000 characters")
    @Column(name = "follow_up_instructions", length = 2000)
    private String followUpInstructions;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Size(max = 100, message = "Discharge condition cannot exceed 100 characters")
    @Column(name = "discharge_condition", length = 100)
    private String dischargeCondition;

    @Size(max = 1000, message = "Restrictions cannot exceed 1000 characters")
    @Column(name = "restrictions", length = 1000)
    private String restrictions;

    @Size(max = 1000, message = "Warning signs cannot exceed 1000 characters")
    @Column(name = "warning_signs_to_report", length = 1000)
    private String warningSignsToReport;

    @Size(max = 100, message = "Created by cannot exceed 100 characters")
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}
