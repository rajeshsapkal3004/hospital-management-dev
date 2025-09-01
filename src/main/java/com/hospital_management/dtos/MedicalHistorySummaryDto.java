package com.hospital_management.dtos;

// MedicalHistorySummaryDto.java


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistorySummaryDto {

    private Long patientId;
    private String patientName;
    private Integer totalRecords;
    private LocalDate firstVisit;
    private LocalDate lastVisit;
    private List<String> chronicConditions;
    private List<String> allergies;
    private List<String> currentMedications;
    private List<String> pastSurgeries;
    private String bloodType;
    private Map<String, Integer> diagnosisFrequency;
    private List<VitalSignsTrendDto> vitalSignsTrends;
    private List<String> familyHistory;
}
