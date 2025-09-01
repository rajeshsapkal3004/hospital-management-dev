package com.hospital_management.dtos;

import com.hospital_management.models.LabTechnician;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class LabTechnicianRegistrationDto extends UserRegistrationDto {

    @NotBlank(message = "Employee ID is required for lab technicians")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    private String employeeId;

    @Pattern(regexp = "^[A-Z0-9-]{5,20}$", message = "Certification number must be 5-20 characters with uppercase letters, numbers, and hyphens")
    private String certificationNumber;

    @NotBlank(message = "Department is required for lab technicians")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @NotBlank(message = "Lab section is required")
    @Size(min = 2, max = 100, message = "Lab section must be between 2 and 100 characters")
    private String labSection;

    @NotNull(message = "Shift is required")
    private LabTechnician.Shift shift;

    @Future(message = "Certification expiry date must be in the future")
    private LocalDate certificationExpiry;

    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @Size(max = 100, message = "Supervisor name cannot exceed 100 characters")
    private String supervisorName;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    private Integer yearsOfExperience;

    private Boolean canApproveResults = false;
    private Boolean canOperateEquipment = true;
    private Boolean isSeniorTechnician = false;

    @Size(max = 500, message = "Qualifications cannot exceed 500 characters")
    private String qualifications;

    private Set<String> specializations;
    private Set<String> certifiedEquipment;
}
