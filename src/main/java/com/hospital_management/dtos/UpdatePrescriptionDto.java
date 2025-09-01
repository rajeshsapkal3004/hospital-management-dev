package com.hospital_management.dtos;

import jakarta.validation.constraints.Size;
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
public class UpdatePrescriptionDto {

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
}