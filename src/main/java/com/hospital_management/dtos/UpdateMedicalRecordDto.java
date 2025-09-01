package com.hospital_management.dtos;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMedicalRecordDto {

    @Size(max = 2000, message = "Chief complaint cannot exceed 2000 characters")
    private String chiefComplaint;

    @Size(max = 3000, message = "History of present illness cannot exceed 3000 characters")
    private String historyOfPresentIllness;

    @Size(max = 2000, message = "Past medical history cannot exceed 2000 characters")
    private String pastMedicalHistory;

    @Size(max = 1000, message = "Family history cannot exceed 1000 characters")
    private String familyHistory;

    @Size(max = 1000, message = "Social history cannot exceed 1000 characters")
    private String socialHistory;

    @Size(max = 1000, message = "Allergies cannot exceed 1000 characters")
    private String allergies;

    @Size(max = 1000, message = "Current medications cannot exceed 1000 characters")
    private String currentMedications;

    @Size(max = 2000, message = "Physical examination cannot exceed 2000 characters")
    private String physicalExamination;

    @Size(max = 3000, message = "Clinical findings cannot exceed 3000 characters")
    private String clinicalFindings;

    @Size(max = 2000, message = "Diagnosis cannot exceed 2000 characters")
    private String diagnosis;

    @Size(max = 1000, message = "ICD codes cannot exceed 1000 characters")
    private String icdCodes;

    @Size(max = 3000, message = "Treatment plan cannot exceed 3000 characters")
    private String treatmentPlan;

    @Size(max = 2000, message = "Procedures cannot exceed 2000 characters")
    private String proceduresPerformed;

    @Size(max = 1000, message = "Lab tests ordered cannot exceed 1000 characters")
    private String labTestsOrdered;

    @Size(max = 1000, message = "Imaging ordered cannot exceed 1000 characters")
    private String imagingOrdered;

    @Size(max = 2000, message = "Follow up instructions cannot exceed 2000 characters")
    private String followUpInstructions;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate followUpDate;

    @Size(max = 3000, message = "Additional notes cannot exceed 3000 characters")
    private String additionalNotes;

    // Vital Signs
    @DecimalMin(value = "35.0", message = "Temperature must be at least 35°C")
    @DecimalMax(value = "45.0", message = "Temperature cannot exceed 45°C")
    private Double temperature;

    @Min(value = 70, message = "Systolic blood pressure must be at least 70")
    @Max(value = 250, message = "Systolic blood pressure cannot exceed 250")
    private Integer bloodPressureSystolic;

    @Min(value = 40, message = "Diastolic blood pressure must be at least 40")
    @Max(value = 150, message = "Diastolic blood pressure cannot exceed 150")
    private Integer bloodPressureDiastolic;

    @Min(value = 40, message = "Heart rate must be at least 40")
    @Max(value = 200, message = "Heart rate cannot exceed 200")
    private Integer heartRate;

    @Min(value = 10, message = "Respiratory rate must be at least 10")
    @Max(value = 40, message = "Respiratory rate cannot exceed 40")
    private Integer respiratoryRate;

    @Min(value = 70, message = "Oxygen saturation must be at least 70%")
    @Max(value = 100, message = "Oxygen saturation cannot exceed 100%")
    private Integer oxygenSaturation;

    @DecimalMin(value = "1.0", message = "Weight must be at least 1 kg")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
    private Double weight;

    @DecimalMin(value = "30.0", message = "Height must be at least 30 cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250 cm")
    private Double height;

    @Size(max = 2000, message = "Discharge summary cannot exceed 2000 characters")
    private String dischargeSummary;
}
