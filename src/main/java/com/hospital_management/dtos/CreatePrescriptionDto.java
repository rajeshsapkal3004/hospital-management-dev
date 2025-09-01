package com.hospital_management.dtos;

import com.hospital_management.models.Prescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrescriptionDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private Long medicalRecordId;
    private Long appointmentId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;

    @Size(max = 2000, message = "Diagnosis cannot exceed 2000 characters")
    private String diagnosis;

    @Size(max = 1000, message = "Patient instructions cannot exceed 1000 characters")
    private String patientInstructions;

    @Size(max = 1000, message = "Pharmacy notes cannot exceed 1000 characters")
    private String pharmacyNotes;

    @Size(max = 500, message = "Additional notes cannot exceed 500 characters")
    private String additionalNotes;

    @Builder.Default
    private Prescription.PrescriptionPriority priority = Prescription.PrescriptionPriority.NORMAL;

    @Builder.Default
    private Boolean isRepeatable = false;

    @Min(value = 0, message = "Repeat count cannot be negative")
    @Max(value = 12, message = "Repeat count cannot exceed 12")
    private Integer repeatCount;

    @NotEmpty(message = "At least one medication is required")
    @Valid
    private List<CreatePrescriptionItemDto> prescriptionItems;
}
