package com.hospital_management.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private LocalDateTime recordedDate;
    private Double temperature; // in Celsius
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private String bloodPressure; // computed: "120/80"
    private Integer heartRate; // beats per minute
    private Integer respiratoryRate; // breaths per minute
    private Integer oxygenSaturation; // percentage
    private Double weight; // in kg
    private Double height; // in cm
    private Double bmi; // computed
    private String painLevel; // 0-10 scale
    private String consciousness; // Alert, Drowsy, etc.
    private String notes;
    private String recordedBy;
    private String recordedByType; // DOCTOR, NURSE, etc.

    // Computed fields
    private String temperatureStatus; // Normal, Fever, etc.
    private String bloodPressureStatus; // Normal, High, Low
    private String heartRateStatus; // Normal, Bradycardia, Tachycardia
    private String bmiCategory; // Underweight, Normal, Overweight, Obese
    private String overallStatus; // Normal, Abnormal

    // Helper methods
    public String getBloodPressure() {
        if (systolicPressure != null && diastolicPressure != null) {
            return systolicPressure + "/" + diastolicPressure;
        }
        return null;
    }

    public String getTemperatureStatus() {
        if (temperature == null) return "Not recorded";
        if (temperature < 36.1) return "Low";
        if (temperature > 37.2) return "Fever";
        return "Normal";
    }

    public String getBloodPressureStatus() {
        if (systolicPressure == null || diastolicPressure == null) return "Not recorded";
        if (systolicPressure >= 140 || diastolicPressure >= 90) return "High";
        if (systolicPressure < 90 || diastolicPressure < 60) return "Low";
        return "Normal";
    }

    public String getHeartRateStatus() {
        if (heartRate == null) return "Not recorded";
        if (heartRate < 60) return "Bradycardia";
        if (heartRate > 100) return "Tachycardia";
        return "Normal";
    }

    public String getBmiCategory() {
        if (bmi == null) return "Not calculated";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
}
