package com.hospital_management.dtos;


import com.hospital_management.models.Nurse;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NurseRegistrationDto extends UserRegistrationDto {

    @NotBlank(message = "License number is required for nurses")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    private String licenseNumber;

    @NotBlank(message = "Employee ID is required for nurses")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    private String employeeId;

    @NotBlank(message = "Department is required for nurses")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    private Nurse.Shift shift;

    @Future(message = "Certification expiry date must be in the future")
    private LocalDate certificationExpiry;

    @NotNull(message = "Nurse type is required")
    private Nurse.NurseType nurseType;

    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @Size(max = 50, message = "Ward assignment cannot exceed 50 characters")
    private String wardAssignment;

    @Size(max = 100, message = "Supervisor name cannot exceed 100 characters")
    private String supervisorName;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    private Integer yearsOfExperience;

    @Size(max = 500, message = "Qualifications cannot exceed 500 characters")
    private String qualifications;

    private Set<String> specializations;
}
