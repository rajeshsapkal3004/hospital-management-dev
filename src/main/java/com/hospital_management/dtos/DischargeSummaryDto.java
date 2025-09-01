package com.hospital_management.dtos;

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
public class DischargeSummaryDto {

    private Long id;
    private Long patientId;
    private Long medicalRecordId;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private String admissionDiagnosis;
    private String dischargeDiagnosis;
    private String treatmentProvided;
    private String dischargeMedications;
    private String followUpInstructions;
    private LocalDate followUpDate;
    private String dischargeCondition;
    private String restrictions;
    private String warningSignsToReport;
    private String createdBy;
    private LocalDateTime createdDate;
}