package com.hospital_management.dtos;


import com.hospital_management.models.Nurse;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NurseDto extends UserDto {
    private String licenseNumber;
    private String employeeId;
    private String department;
    private Nurse.Shift shift;
    private LocalDate certificationExpiry;
    private Nurse.NurseType nurseType;
    private LocalDate hireDate;
    private String wardAssignment;
    private String supervisorName;
    private Integer yearsOfExperience;
    private boolean isHeadNurse;
    private boolean isAvailableForDuty;
    private boolean canAdministerMedication;
    private String qualifications;
    private Set<String> specializations;
}
