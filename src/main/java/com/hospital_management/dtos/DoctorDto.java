package com.hospital_management.dtos;



import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DoctorDto extends UserDto {
    private String licenseNumber;
    private String specialization;
    private String department;
    private LocalDate licenseExpiry;
    private String npiNumber;
    private String employeeId;
    private LocalDate hireDate;
    private BigDecimal consultationFee;
    private Integer yearsOfExperience;
    private String qualifications;
    private String bio;
    private boolean isAvailableForConsultation;
    private boolean isEmergencyContact;
    private String roomNumber;
    private Set<String> certifications;
}
