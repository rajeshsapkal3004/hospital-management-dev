package com.hospital_management.dtos;

import com.hospital_management.models.LabResult;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLabResultDto {

    @Size(max = 2000, message = "Results cannot exceed 2000 characters")
    private String results;

    @Size(max = 500, message = "Normal range cannot exceed 500 characters")
    private String normalRange;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    private LabResult.ResultStatus status;

    private LabResult.ResultFlag resultFlag;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Interpretation cannot exceed 1000 characters")
    private String interpretation;

    private Boolean isCritical;

    private Boolean isAbnormal;

    private Map<String, String> testValues;
}