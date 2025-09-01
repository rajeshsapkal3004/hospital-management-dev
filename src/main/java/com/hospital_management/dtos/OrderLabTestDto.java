package com.hospital_management.dtos;

import com.hospital_management.models.LabTest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLabTestDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotBlank(message = "Test type is required")
    @Size(max = 100, message = "Test type cannot exceed 100 characters")
    private String testType;

    @NotBlank(message = "Test name is required")
    @Size(max = 200, message = "Test name cannot exceed 200 characters")
    private String testName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledDate;

    @Size(max = 50, message = "Sample type cannot exceed 50 characters")
    private String sampleType;

    @Size(max = 1000, message = "Instructions cannot exceed 1000 characters")
    private String instructions;

    @Builder.Default
    private LabTest.TestUrgency urgency = LabTest.TestUrgency.ROUTINE;

    @Size(max = 500, message = "Clinical info cannot exceed 500 characters")
    private String clinicalInfo;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Builder.Default
    private Boolean isFastingRequired = false;

    @Builder.Default
    private Boolean isHomeCollection = false;

    @Size(max = 200, message = "Collection address cannot exceed 200 characters")
    private String collectionAddress;
}