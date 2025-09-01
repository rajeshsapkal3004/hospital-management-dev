package com.hospital_management.dtos;



import com.hospital_management.models.LabResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResultDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long labTestId;
    private String testType;
    private String testName;
    private LocalDateTime testDate;
    private String sampleType;
    private LocalDateTime sampleCollectedDate;
    private String results;
    private String normalRange;
    private String unit;
    private LabResult.ResultStatus status;
    private LabResult.ResultFlag resultFlag;
    private String notes;
    private String interpretation;
    private Long technicianId;
    private String technicianName;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime verifiedDate;
    private Long verifiedBy;
    private String verifiedByName;
    private Boolean isCritical;
    private Boolean isAbnormal;
    private String referenceNumber;
    private String reportFilePath;
    private Map<String, String> testValues;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Computed fields
    private String statusDisplayName;
    private String resultFlagDisplayName;
    private Boolean isCompleted;
    private Boolean requiresAttention;
    private String reportUrl;

    // Helper methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public String getResultFlagDisplayName() {
        return resultFlag != null ? resultFlag.getDisplayName() : null;
    }

    public Boolean getIsCompleted() {
        return status == LabResult.ResultStatus.COMPLETED ||
                status == LabResult.ResultStatus.VERIFIED;
    }

    public Boolean getRequiresAttention() {
        return isCritical != null && isCritical ||
                isAbnormal != null && isAbnormal ||
                resultFlag == LabResult.ResultFlag.CRITICAL_HIGH ||
                resultFlag == LabResult.ResultFlag.CRITICAL_LOW;
    }
}

