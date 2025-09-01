package com.hospital_management.dtos;


import com.hospital_management.models.LabTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String testType;
    private String testName;
    private LocalDateTime orderDate;
    private LocalDateTime scheduledDate;
    private String sampleType;
    private String instructions;
    private LabTest.TestStatus status;
    private LabTest.TestUrgency urgency;
    private String orderNumber;
    private String clinicalInfo;
    private LocalDateTime sampleCollectedDate;
    private Long sampleCollectedBy;
    private LocalDateTime estimatedCompletionDate;
    private String notes;
    private Boolean isFastingRequired;
    private Boolean isHomeCollection;
    private String collectionAddress;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Computed fields
    private String statusDisplayName;
    private String urgencyDisplayName;
    private Boolean canCollectSample;
    private Boolean isCompleted;
    private List<LabResultDto> results;

    // Helper methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public String getUrgencyDisplayName() {
        return urgency != null ? urgency.getDisplayName() : null;
    }

    public Boolean getCanCollectSample() {
        return status == LabTest.TestStatus.ORDERED ||
                status == LabTest.TestStatus.SCHEDULED;
    }

    public Boolean getIsCompleted() {
        return status == LabTest.TestStatus.COMPLETED;
    }
}
