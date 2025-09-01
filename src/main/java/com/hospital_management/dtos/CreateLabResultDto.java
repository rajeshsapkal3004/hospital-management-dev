package com.hospital_management.dtos;

import com.hospital_management.models.LabResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLabResultDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long labTestId;

    @NotBlank(message = "Test type is required")
    @Size(max = 100, message = "Test type cannot exceed 100 characters")
    private String testType;

    @NotBlank(message = "Test name is required")
    @Size(max = 200, message = "Test name cannot exceed 200 characters")
    private String testName;

    @NotNull(message = "Test date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime testDate;

    @Size(max = 50, message = "Sample type cannot exceed 50 characters")
    private String sampleType;

    @Size(max = 2000, message = "Results cannot exceed 2000 characters")
    private String results;

    @Size(max = 500, message = "Normal range cannot exceed 500 characters")
    private String normalRange;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    @Builder.Default
    private LabResult.ResultStatus status = LabResult.ResultStatus.COMPLETED;

    private LabResult.ResultFlag resultFlag;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Interpretation cannot exceed 1000 characters")
    private String interpretation;

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotBlank(message = "Technician name is required")
    @Size(max = 100, message = "Technician name cannot exceed 100 characters")
    private String technicianName;

    @Builder.Default
    private Boolean isCritical = false;

    @Builder.Default
    private Boolean isAbnormal = false;

    private Map<String, String> testValues;
}