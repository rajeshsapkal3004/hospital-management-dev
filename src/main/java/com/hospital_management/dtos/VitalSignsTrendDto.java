package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsTrendDto {

    private LocalDate date;
    private Double temperature;
    private String bloodPressure;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Integer oxygenSaturation;
    private Double weight;
    private Double height;
    private Double bmi;
}